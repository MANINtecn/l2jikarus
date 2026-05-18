package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.time.SchedulingPattern;
import net.sf.l2jdev.commons.time.TimeUtil;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.DevelopmentConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.data.SpawnTable;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.SpawnData;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.RaidBossStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;

public class DatabaseSpawnManager
{
	private static final Logger LOGGER = Logger.getLogger(DatabaseSpawnManager.class.getName());
	protected final Map<Integer, Npc> _npcs = new ConcurrentHashMap<>();
	protected final Map<Integer, Spawn> _spawns = new ConcurrentHashMap<>();
	protected final Map<Integer, StatSet> _storedInfo = new ConcurrentHashMap<>();
	protected final Map<Integer, ScheduledFuture<?>> _schedules = new ConcurrentHashMap<>();

	protected DatabaseSpawnManager()
	{
		this.load();
	}

	public void load()
	{
		if (!DevelopmentConfig.NO_SPAWNS)
		{
			if (!this._spawns.isEmpty())
			{
				for (Spawn spawn : this._spawns.values())
				{
					this.deleteSpawn(spawn, false);
				}
			}

			this._npcs.clear();
			this._spawns.clear();
			this._storedInfo.clear();
			this._schedules.clear();

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM npc_respawns"); ResultSet rset = statement.executeQuery();)
			{
				while (rset.next())
				{
					int id = rset.getInt("id");
					NpcTemplate template = this.getValidTemplate(id);
					if (template != null)
					{
						Spawn spawn = new Spawn(template);
						spawn.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
						spawn.setAmount(1);
						spawn.setHeading(rset.getInt("heading"));
						List<NpcSpawnTemplate> spawns = SpawnData.getInstance().getNpcSpawns(npc -> npc.getId() == template.getId() && npc.hasDBSave());
						if (spawns.isEmpty())
						{
							LOGGER.warning(this.getClass().getSimpleName() + ": Couldn't find spawn declaration for npc: " + template.getId() + " - " + template.getName());
							this.deleteSpawn(spawn, true);
						}
						else if (spawns.size() > 1)
						{
							LOGGER.warning(this.getClass().getSimpleName() + ": Found multiple database spawns for npc: " + template.getId() + " - " + template.getName() + " " + spawns);
						}
						else
						{
							NpcSpawnTemplate spawnTemplate = spawns.get(0);
							spawn.setSpawnTemplate(spawnTemplate);
							int respawn = 0;
							int respawnRandom = 0;
							SchedulingPattern respawnPattern = null;
							if (spawnTemplate.getRespawnTime() != null)
							{
								respawn = (int) spawnTemplate.getRespawnTime().getSeconds();
							}

							if (spawnTemplate.getRespawnTimeRandom() != null)
							{
								respawnRandom = (int) spawnTemplate.getRespawnTimeRandom().getSeconds();
							}

							if (spawnTemplate.getRespawnPattern() != null)
							{
								respawnPattern = spawnTemplate.getRespawnPattern();
							}

							if (respawn <= 0 && respawnPattern == null)
							{
								spawn.stopRespawn();
								LOGGER.warning(this.getClass().getSimpleName() + ": Found database spawns without respawn for npc: " + template.getId() + " - " + template.getName() + " " + spawnTemplate);
							}
							else
							{
								spawn.setRespawnDelay(respawn, respawnRandom);
								spawn.setRespawnPattern(respawnPattern);
								spawn.startRespawn();
								this.addNewSpawn(spawn, rset.getLong("respawnTime"), rset.getDouble("currentHp"), rset.getDouble("currentMp"), false);
							}
						}
					}
					else
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": Could not load npc #" + rset.getInt("id") + " from DB");

						try (Connection con2 = DatabaseFactory.getConnection(); PreparedStatement statement2 = con2.prepareStatement("DELETE FROM npc_respawns WHERE id=?");)
						{
							statement2.setInt(1, id);
							statement2.execute();
						}
						catch (Exception var19)
						{
							LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not remove npc #" + id + " from DB: ", var19);
						}
					}
				}

				LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._npcs.size() + " instances.");
				LOGGER.info(this.getClass().getSimpleName() + ": Scheduled " + this._schedules.size() + " instances.");
			}
			catch (SQLException var23)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not load npc_respawns table", var23);
			}
			catch (Exception var24)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing DatabaseSpawnManager: ", var24);
			}
		}
	}

	private void scheduleSpawn(int npcId)
	{
		Npc npc = this._spawns.get(npcId).doSpawn();
		if (npc != null)
		{
			npc.setDBStatus(RaidBossStatus.ALIVE);
			StatSet info = new StatSet();
			info.set("currentHP", npc.getCurrentHp());
			info.set("currentMP", npc.getCurrentMp());
			info.set("respawnTime", 0);
			this._storedInfo.put(npcId, info);
			this._npcs.put(npcId, npc);
			LOGGER.info(this.getClass().getSimpleName() + ": Spawning NPC " + npc.getName());
		}

		this._schedules.remove(npcId);
	}

	public void updateStatus(Npc npc, boolean isNpcDead)
	{
		StatSet info = this._storedInfo.get(npc.getId());
		if (info != null)
		{
			if (isNpcDead)
			{
				npc.setDBStatus(RaidBossStatus.DEAD);
				SchedulingPattern respawnPattern = npc.getSpawn().getRespawnPattern();
				int respawnMinDelay;
				int respawnMaxDelay;
				int respawnDelay;
				long respawnTime;
				if (respawnPattern != null)
				{
					respawnTime = respawnPattern.next(System.currentTimeMillis());
					respawnMinDelay = (int) (respawnTime - System.currentTimeMillis());
					respawnMaxDelay = respawnMinDelay;
					respawnDelay = respawnMinDelay;
				}
				else
				{
					respawnMinDelay = (int) (npc.getSpawn().getRespawnMinDelay() * NpcConfig.RAID_MIN_RESPAWN_MULTIPLIER);
					respawnMaxDelay = (int) (npc.getSpawn().getRespawnMaxDelay() * NpcConfig.RAID_MAX_RESPAWN_MULTIPLIER);
					respawnDelay = Rnd.get(respawnMinDelay, respawnMaxDelay);
					respawnTime = System.currentTimeMillis() + respawnDelay;
				}

				info.set("currentHP", npc.getMaxHp());
				info.set("currentMP", npc.getMaxMp());
				info.set("respawnTime", respawnTime);
				if (!this._schedules.containsKey(npc.getId()) && (respawnMinDelay > 0 || respawnMaxDelay > 0) || respawnPattern != null)
				{
					LOGGER.info(this.getClass().getSimpleName() + ": Updated " + npc.getName() + " respawn time to " + TimeUtil.getDateTimeString(respawnTime));
					this._schedules.put(npc.getId(), ThreadPool.schedule(() -> this.scheduleSpawn(npc.getId()), respawnDelay));
					this.updateDb();
				}
			}
			else
			{
				npc.setDBStatus(RaidBossStatus.ALIVE);
				info.set("currentHP", npc.getCurrentHp());
				info.set("currentMP", npc.getCurrentMp());
				info.set("respawnTime", 0);
			}

			this._storedInfo.put(npc.getId(), info);
		}
	}

	public void addNewSpawn(Spawn spawn, long respawnTime, double currentHP, double currentMP, boolean storeInDb)
	{
		if (spawn != null)
		{
			if (!this._spawns.containsKey(spawn.getId()))
			{
				int npcId = spawn.getId();
				long time = System.currentTimeMillis();
				SpawnTable.getInstance().addSpawn(spawn);
				if (respawnTime != 0L && time <= respawnTime)
				{
					long spawnTime = respawnTime - System.currentTimeMillis();
					this._schedules.put(npcId, ThreadPool.schedule(() -> this.scheduleSpawn(npcId), spawnTime));
				}
				else
				{
					Npc npc = spawn.doSpawn();
					if (npc != null)
					{
						ThreadPool.schedule(() -> {
							npc.setCurrentHp(currentHP);
							npc.setCurrentMp(currentMP, false);
						}, 100L);
						npc.setDBStatus(RaidBossStatus.ALIVE);
						this._npcs.put(npcId, npc);
						StatSet info = new StatSet();
						info.set("currentHP", currentHP);
						info.set("currentMP", currentMP);
						info.set("respawnTime", 0);
						this._storedInfo.put(npcId, info);
					}
				}

				this._spawns.put(npcId, spawn);
				if (storeInDb)
				{
					try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO npc_respawns (id, x, y, z, heading, respawnTime, currentHp, currentMp) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");)
					{
						statement.setInt(1, spawn.getId());
						statement.setInt(2, spawn.getX());
						statement.setInt(3, spawn.getY());
						statement.setInt(4, spawn.getZ());
						statement.setInt(5, spawn.getHeading());
						statement.setLong(6, respawnTime);
						statement.setDouble(7, currentHP);
						statement.setDouble(8, currentMP);
						statement.execute();
					}
					catch (Exception var20)
					{
						LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not store npc #" + npcId + " in the DB: ", var20);
					}
				}
			}
		}
	}

	public Npc addNewSpawn(Spawn spawn, boolean storeInDb)
	{
		if (spawn == null)
		{
			return null;
		}
		int npcId = spawn.getId();
		Spawn existingSpawn = this._spawns.get(npcId);
		if (existingSpawn != null)
		{
			return existingSpawn.getLastSpawn();
		}
		SpawnTable.getInstance().addSpawn(spawn);
		Npc npc = spawn.doSpawn();
		if (npc == null)
		{
			throw new NullPointerException();
		}
		npc.setDBStatus(RaidBossStatus.ALIVE);
		StatSet info = new StatSet();
		info.set("currentHP", npc.getMaxHp());
		info.set("currentMP", npc.getMaxMp());
		info.set("respawnTime", 0);
		this._npcs.put(npcId, npc);
		this._storedInfo.put(npcId, info);
		this._spawns.put(npcId, spawn);
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO npc_respawns (id, x, y, z, heading, respawnTime, currentHp, currentMp) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");)
			{
				statement.setInt(1, spawn.getId());
				statement.setInt(2, spawn.getX());
				statement.setInt(3, spawn.getY());
				statement.setInt(4, spawn.getZ());
				statement.setInt(5, spawn.getHeading());
				statement.setLong(6, 0L);
				statement.setDouble(7, npc.getMaxHp());
				statement.setDouble(8, npc.getMaxMp());
				statement.execute();
			}
			catch (Exception var15)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not store npc #" + npcId + " in the DB: ", var15);
			}
		}

		return npc;
	}

	public void deleteSpawn(Spawn spawn, boolean updateDb)
	{
		if (spawn != null)
		{
			int npcId = spawn.getId();
			this._spawns.remove(npcId);
			this._npcs.remove(npcId);
			this._storedInfo.remove(npcId);
			ScheduledFuture<?> task = this._schedules.remove(npcId);
			if (task != null)
			{
				task.cancel(true);
			}

			if (updateDb)
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM npc_respawns WHERE id = ?");)
				{
					ps.setInt(1, npcId);
					ps.execute();
				}
				catch (Exception var13)
				{
					LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not remove npc #" + npcId + " from DB: ", var13);
				}
			}

			SpawnTable.getInstance().removeSpawn(spawn);
		}
	}

	private void updateDb()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE npc_respawns SET respawnTime = ?, currentHP = ?, currentMP = ? WHERE id = ?");)
		{
			for (Entry<Integer, StatSet> entry : this._storedInfo.entrySet())
			{
				Integer npcId = entry.getKey();
				if (npcId != null)
				{
					Npc npc = this._npcs.get(npcId);
					if (npc != null)
					{
						if (npc.getDBStatus() == RaidBossStatus.ALIVE)
						{
							this.updateStatus(npc, false);
						}

						StatSet info = entry.getValue();
						if (info != null)
						{
							try
							{
								statement.setLong(1, info.getLong("respawnTime"));
								statement.setDouble(2, npc.isDead() ? npc.getMaxHp() : info.getDouble("currentHP"));
								statement.setDouble(3, npc.isDead() ? npc.getMaxMp() : info.getDouble("currentMP"));
								statement.setInt(4, npcId);
								statement.executeUpdate();
								statement.clearParameters();
							}
							catch (SQLException var11)
							{
								LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not update npc_respawns table ", var11);
							}
						}
					}
				}
			}
		}
		catch (SQLException var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": SQL error while updating database spawn to database: ", var14);
		}
	}

	public String[] getAllNpcsStatus()
	{
		String[] msg = new String[this._npcs == null ? 0 : this._npcs.size()];
		if (this._npcs == null)
		{
			msg[0] = "None";
			return msg;
		}
		int index = 0;

		for (Npc npc : this._npcs.values())
		{
			msg[index++] = npc.getName() + ": " + npc.getDBStatus().name();
		}

		return msg;
	}

	public String getNpcsStatus(int npcId)
	{
		String msg = "NPC Status..." + System.lineSeparator();
		if (this._npcs == null)
		{
			return msg + "None";
		}
		if (this._npcs.containsKey(npcId))
		{
			Npc npc = this._npcs.get(npcId);
			msg = msg + npc.getName() + ": " + npc.getDBStatus().name();
		}

		return msg;
	}

	public RaidBossStatus getStatus(int npcId)
	{
		if (this._npcs.containsKey(npcId))
		{
			return this._npcs.get(npcId).getDBStatus();
		}
		return this._schedules.containsKey(npcId) ? RaidBossStatus.DEAD : RaidBossStatus.UNDEFINED;
	}

	public NpcTemplate getValidTemplate(int npcId)
	{
		return NpcData.getInstance().getTemplate(npcId);
	}

	public void notifySpawnNightNpc(Npc npc)
	{
		StatSet info = new StatSet();
		info.set("currentHP", npc.getCurrentHp());
		info.set("currentMP", npc.getCurrentMp());
		info.set("respawnTime", 0);
		npc.setDBStatus(RaidBossStatus.ALIVE);
		this._storedInfo.put(npc.getId(), info);
		this._npcs.put(npc.getId(), npc);
	}

	public boolean isDefined(int npcId)
	{
		return this._spawns.containsKey(npcId);
	}

	public Npc getNpc(int id)
	{
		return this._npcs.get(id);
	}

	public Map<Integer, Npc> getNpcs()
	{
		return this._npcs;
	}

	public Map<Integer, Spawn> getSpawns()
	{
		return this._spawns;
	}

	public Map<Integer, StatSet> getStoredInfo()
	{
		return this._storedInfo;
	}

	public void cleanUp()
	{
		this.updateDb();
		this._npcs.clear();

		for (ScheduledFuture<?> shedule : this._schedules.values())
		{
			shedule.cancel(true);
		}

		this._schedules.clear();
		this._storedInfo.clear();
		this._spawns.clear();
	}

	public static DatabaseSpawnManager getInstance()
	{
		return DatabaseSpawnManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final DatabaseSpawnManager INSTANCE = new DatabaseSpawnManager();
	}
}
