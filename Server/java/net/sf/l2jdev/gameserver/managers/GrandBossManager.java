package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.instance.GrandBoss;

public class GrandBossManager
{
	public static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";
	public static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";
	protected static final Logger LOGGER = Logger.getLogger(GrandBossManager.class.getName());
	protected static Map<Integer, GrandBoss> _bosses = new ConcurrentHashMap<>();
	protected static Map<Integer, StatSet> _storedInfo = new HashMap<>();
	private final Map<Integer, Integer> _bossStatus = new HashMap<>();

	protected GrandBossManager()
	{
		this.init();
	}

	private void init()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT * from grandboss_data ORDER BY boss_id");)
		{
			while (rs.next())
			{
				int bossId = rs.getInt("boss_id");
				if (NpcData.getInstance().getTemplate(bossId) != null)
				{
					StatSet info = new StatSet();
					info.set("loc_x", rs.getInt("loc_x"));
					info.set("loc_y", rs.getInt("loc_y"));
					info.set("loc_z", rs.getInt("loc_z"));
					info.set("heading", rs.getInt("heading"));
					info.set("respawn_time", rs.getLong("respawn_time"));
					info.set("currentHP", rs.getDouble("currentHP"));
					info.set("currentMP", rs.getDouble("currentMP"));
					int status = rs.getInt("status");
					this._bossStatus.put(bossId, status);
					_storedInfo.put(bossId, info);
					LOGGER.info(this.getClass().getSimpleName() + ": " + NpcData.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status is " + status);
					if (status > 0)
					{
						LOGGER.info(this.getClass().getSimpleName() + ": Next spawn date of " + NpcData.getInstance().getTemplate(bossId).getName() + " is " + new Date(info.getLong("respawn_time")));
					}
				}
				else
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Could not find GrandBoss NPC template for " + bossId);
				}
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _storedInfo.size() + " instances.");
		}
		catch (SQLException var13)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not load grandboss_data table: " + var13.getMessage(), var13);
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing GrandBossManager: " + var14.getMessage(), var14);
		}

		ThreadPool.scheduleAtFixedRate(this::storeMe, 300000L, 300000L);
	}

	public int getStatus(int bossId)
	{
		return !this._bossStatus.containsKey(bossId) ? -1 : this._bossStatus.get(bossId);
	}

	public void setStatus(int bossId, int status)
	{
		this._bossStatus.put(bossId, status);
		LOGGER.info(this.getClass().getSimpleName() + ": Updated " + NpcData.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status to " + status + ".");
		this.updateDb(bossId, true);
	}

	public void addBoss(GrandBoss boss)
	{
		if (boss != null)
		{
			_bosses.put(boss.getId(), boss);
		}
	}

	public void addBoss(int bossId, GrandBoss boss)
	{
		if (boss != null)
		{
			_bosses.put(bossId, boss);
		}
	}

	public GrandBoss getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}

	public StatSet getStatSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}

	public void setStatSet(int bossId, StatSet info)
	{
		_storedInfo.put(bossId, info);
		this.updateDb(bossId, false);
	}

	public boolean storeMe()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				for (Entry<Integer, StatSet> e : _storedInfo.entrySet())
				{
					GrandBoss boss = _bosses.get(e.getKey());
					StatSet info = e.getValue();
					if (boss != null && info != null)
					{
						try (PreparedStatement update = con.prepareStatement("UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?"))
						{
							update.setInt(1, boss.getX());
							update.setInt(2, boss.getY());
							update.setInt(3, boss.getZ());
							update.setInt(4, boss.getHeading());
							update.setLong(5, info.getLong("respawn_time"));
							double hp = boss.getCurrentHp();
							double mp = boss.getCurrentMp();
							if (boss.isDead())
							{
								hp = boss.getMaxHp();
								mp = boss.getMaxMp();
							}

							update.setDouble(6, hp);
							update.setDouble(7, mp);
							update.setInt(8, this._bossStatus.get(e.getKey()));
							update.setInt(9, e.getKey());
							update.executeUpdate();
							update.clearParameters();
						}
					}
					else
					{
						try (PreparedStatement update = con.prepareStatement("UPDATE grandboss_data set status = ? where boss_id = ?"))
						{
							update.setInt(1, this._bossStatus.get(e.getKey()));
							update.setInt(2, e.getKey());
							update.executeUpdate();
							update.clearParameters();
						}
					}
				}
			}

			return true;
		}
		catch (SQLException var17)
		{
			LOGGER.log(Level.WARNING, "Couldn't store grandbosses to database: " + var17.getMessage(), var17);
			return false;
		}
	}

	private void updateDb(int bossId, boolean statusOnly)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			GrandBoss boss = _bosses.get(bossId);
			StatSet info = _storedInfo.get(bossId);
			if (!statusOnly && boss != null && info != null)
			{
				try (PreparedStatement ps = con.prepareStatement("UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?"))
				{
					ps.setInt(1, boss.getX());
					ps.setInt(2, boss.getY());
					ps.setInt(3, boss.getZ());
					ps.setInt(4, boss.getHeading());
					ps.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}

					ps.setDouble(6, hp);
					ps.setDouble(7, mp);
					ps.setInt(8, this._bossStatus.get(bossId));
					ps.setInt(9, bossId);
					ps.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement ps = con.prepareStatement("UPDATE grandboss_data set status = ? where boss_id = ?"))
				{
					ps.setInt(1, this._bossStatus.get(bossId));
					ps.setInt(2, bossId);
					ps.executeUpdate();
				}
			}
		}
		catch (SQLException var17)
		{
			LOGGER.log(Level.WARNING, "Couldn't update grandbosses to database:" + var17.getMessage(), var17);
		}
	}

	public void cleanUp()
	{
		this.storeMe();
		_bosses.clear();
		_storedInfo.clear();
		this._bossStatus.clear();
	}

	public static GrandBossManager getInstance()
	{
		return GrandBossManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final GrandBossManager INSTANCE = new GrandBossManager();
	}
}
