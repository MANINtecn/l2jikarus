package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.ConfigReader;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.TowerSpawn;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.castlewar.MercenaryCastleWarCastleSiegeHudInfo;

public class SiegeManager
{
	private static final Logger LOGGER = Logger.getLogger(SiegeManager.class.getName());
	public static final String SIEGE_CONFIG_FILE = "./config/Siege.ini";
	private final Map<Integer, List<TowerSpawn>> _controlTowers = new HashMap<>();
	private final Map<Integer, List<TowerSpawn>> _flameTowers = new HashMap<>();
	private final Map<Integer, TowerSpawn> _relicTowers = new HashMap<>();
	private int _siegeCycle = 2;
	private int _attackerMaxClans = 500;
	private int _attackerRespawnDelay = 0;
	private int _defenderMaxClans = 500;
	private int _flagMaxCount = 1;
	private int _siegeClanMinLevel = 5;
	private int _siegeLength = 120;
	private int _bloodAllianceReward = 0;

	protected SiegeManager()
	{
		this.load();
	}

	public void addSiegeSkills(Player character)
	{
		for (Skill sk : SkillData.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.addSkill(sk, false);
		}
	}

	public boolean checkIsRegistered(Clan clan, int castleid)
	{
		if (clan == null)
		{
			return false;
		}
		else if (clan.getCastleId() > 0)
		{
			return true;
		}
		else
		{
			boolean register = false;

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");)
			{
				statement.setInt(1, clan.getId());
				statement.setInt(2, castleid);

				try (ResultSet rs = statement.executeQuery())
				{
					if (rs.next())
					{
						register = true;
					}
				}
			}
			catch (Exception var15)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: checkIsRegistered(): " + var15.getMessage(), var15);
			}

			return register;
		}
	}

	public void removeSiegeSkills(Player character)
	{
		for (Skill sk : SkillData.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.removeSkill(sk);
		}
	}

	private void load()
	{
		ConfigReader siegeConfig = new ConfigReader("./config/Siege.ini");
		this._siegeCycle = siegeConfig.getInt("SiegeCycle", 2);
		this._attackerMaxClans = siegeConfig.getInt("AttackerMaxClans", 500);
		this._attackerRespawnDelay = siegeConfig.getInt("AttackerRespawn", 0);
		this._defenderMaxClans = siegeConfig.getInt("DefenderMaxClans", 500);
		this._flagMaxCount = siegeConfig.getInt("MaxFlags", 1);
		this._siegeClanMinLevel = siegeConfig.getInt("SiegeClanMinLevel", 5);
		this._siegeLength = siegeConfig.getInt("SiegeLength", 120);
		this._bloodAllianceReward = siegeConfig.getInt("BloodAllianceReward", 1);

		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			List<TowerSpawn> controlTowers = new ArrayList<>();

			for (int i = 1; i < 255; i++)
			{
				String configKey = castle.getName() + "ControlTower" + i;
				if (!siegeConfig.containsKey(configKey))
				{
					break;
				}

				StringTokenizer st = new StringTokenizer(siegeConfig.getString(configKey, ""), ",");

				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int npcId = Integer.parseInt(st.nextToken());
					controlTowers.add(new TowerSpawn(npcId, new Location(x, y, z)));
				}
				catch (Exception var14)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Error while loading control tower(s) for " + castle.getName() + " castle.");
				}
			}

			List<TowerSpawn> flameTowers = new ArrayList<>();

			for (int i = 1; i < 255; i++)
			{
				String configKey = castle.getName() + "FlameTower" + i;
				if (!siegeConfig.containsKey(configKey))
				{
					break;
				}

				StringTokenizer st = new StringTokenizer(siegeConfig.getString(configKey, ""), ",");

				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int npcId = Integer.parseInt(st.nextToken());
					List<Integer> zoneList = new ArrayList<>();

					while (st.hasMoreTokens())
					{
						zoneList.add(Integer.parseInt(st.nextToken()));
					}

					flameTowers.add(new TowerSpawn(npcId, new Location(x, y, z), zoneList));
				}
				catch (Exception var15)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Error while loading flame tower(s) for " + castle.getName() + " castle.");
				}
			}

			this._controlTowers.put(castle.getResidenceId(), controlTowers);
			this._flameTowers.put(castle.getResidenceId(), flameTowers);
			if (castle.getOwnerId() != 0)
			{
				this.loadTrapUpgrade(castle.getResidenceId());
			}
		}

		String[] relics = siegeConfig.getString("Relic", null).split(";");

		for (String elem : relics)
		{
			String[] s = elem.split(",");
			int castleId = Integer.parseInt(s[0]);
			int npcId = Integer.parseInt(s[1]);
			Location loc = new Location(Integer.parseInt(s[2]), Integer.parseInt(s[3]), Integer.parseInt(s[4]));
			TowerSpawn towerSpawn = new TowerSpawn(npcId, loc);
			this._relicTowers.put(castleId, towerSpawn);
		}
	}

	public TowerSpawn getRelicTowers(int castleId)
	{
		return this._relicTowers.get(castleId);
	}

	public List<TowerSpawn> getControlTowers(int castleId)
	{
		return this._controlTowers.get(castleId);
	}

	public List<TowerSpawn> getFlameTowers(int castleId)
	{
		return this._flameTowers.get(castleId);
	}

	public int getSiegeCycle()
	{
		return this._siegeCycle;
	}

	public int getAttackerMaxClans()
	{
		return this._attackerMaxClans;
	}

	public int getAttackerRespawnDelay()
	{
		return this._attackerRespawnDelay;
	}

	public int getDefenderMaxClans()
	{
		return this._defenderMaxClans;
	}

	public int getFlagMaxCount()
	{
		return this._flagMaxCount;
	}

	public Siege getSiege(ILocational loc)
	{
		return this.getSiege(loc.getX(), loc.getY(), loc.getZ());
	}

	public Siege getSiege(WorldObject activeObject)
	{
		return this.getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
			}
		}

		return null;
	}

	public int getSiegeClanMinLevel()
	{
		return this._siegeClanMinLevel;
	}

	public int getSiegeLength()
	{
		return this._siegeLength;
	}

	public int getBloodAllianceReward()
	{
		return this._bloodAllianceReward;
	}

	public Collection<Siege> getSieges()
	{
		List<Siege> sieges = new LinkedList<>();

		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}

		return sieges;
	}

	public Siege getSiege(int castleId)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getResidenceId() == castleId)
			{
				return castle.getSiege();
			}
		}

		return null;
	}

	private void loadTrapUpgrade(int castleId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_trapupgrade WHERE castleId=?");)
		{
			ps.setInt(1, castleId);

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					this._flameTowers.get(castleId).get(rs.getInt("towerIndex")).setUpgradeLevel(rs.getInt("level"));
				}
			}
		}
		catch (Exception var13)
		{
			LOGGER.log(Level.WARNING, "Exception: loadTrapUpgrade(): " + var13.getMessage(), var13);
		}
	}

	public void sendSiegeInfo(Player player)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			int diff = (int) (castle.getSiegeDate().getTimeInMillis() - System.currentTimeMillis());
			if (diff > 0 && diff < 86400000 || castle.getSiege().isInProgress())
			{
				player.sendPacket(new MercenaryCastleWarCastleSiegeHudInfo(castle.getResidenceId()));
			}
		}
	}

	public void sendSiegeInfo(Player player, int castleId)
	{
		player.sendPacket(new MercenaryCastleWarCastleSiegeHudInfo(castleId));
	}

	public static SiegeManager getInstance()
	{
		return SiegeManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SiegeManager INSTANCE = new SiegeManager();
	}
}
