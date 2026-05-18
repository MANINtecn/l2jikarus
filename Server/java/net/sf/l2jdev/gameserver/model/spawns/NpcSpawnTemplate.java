package net.sf.l2jdev.gameserver.model.spawns;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.time.SchedulingPattern;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.SpawnTable;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.ChanceLocation;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.MinionHolder;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.IParameterized;
import net.sf.l2jdev.gameserver.model.zone.type.BannedSpawnTerritory;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;

public class NpcSpawnTemplate implements Cloneable, IParameterized<StatSet>
{
	private static final Logger LOGGER = Logger.getLogger(NpcSpawnTemplate.class.getName());
	private final int _id;
	private final int _count;
	private final Duration _respawnTime;
	private final Duration _respawnTimeRandom;
	private final SchedulingPattern _respawnPattern;
	private final int _chaseRange;
	private List<ChanceLocation> _locations;
	private SpawnTerritory _zone;
	private StatSet _parameters;
	private final boolean _spawnAnimation;
	private final boolean _saveInDB;
	private final String _dbName;
	private List<MinionHolder> _minions;
	private final SpawnTemplate _spawnTemplate;
	private final SpawnGroup _group;
	private final Set<Npc> _spawnedNpcs = ConcurrentHashMap.newKeySet();

	private NpcSpawnTemplate(NpcSpawnTemplate template)
	{
		this._spawnTemplate = template._spawnTemplate;
		this._group = template._group;
		this._id = template._id;
		this._count = template._count;
		this._respawnTime = template._respawnTime;
		this._respawnTimeRandom = template._respawnTimeRandom;
		this._respawnPattern = template._respawnPattern;
		this._chaseRange = template._chaseRange;
		this._spawnAnimation = template._spawnAnimation;
		this._saveInDB = template._saveInDB;
		this._dbName = template._dbName;
		this._locations = template._locations;
		this._zone = template._zone;
		this._parameters = template._parameters;
		this._minions = template._minions;
	}

	public NpcSpawnTemplate(SpawnTemplate spawnTemplate, SpawnGroup group, StatSet set)
	{
		this._spawnTemplate = spawnTemplate;
		this._group = group;
		this._id = set.getInt("id");
		this._count = set.getInt("count", 1);
		this._respawnTime = set.getDuration("respawnTime", null);
		this._respawnTimeRandom = set.getDuration("respawnRandom", null);
		String pattern = set.getString("respawnPattern", null);
		this._respawnPattern = pattern != null && !pattern.isEmpty() ? new SchedulingPattern(pattern) : null;
		this._chaseRange = set.getInt("chaseRange", 0);
		this._spawnAnimation = set.getBoolean("spawnAnimation", false);
		this._saveInDB = set.getBoolean("dbSave", false);
		this._dbName = set.getString("dbName", null);
		this._parameters = this.mergeParameters(spawnTemplate, group);
		int x = set.getInt("x", Integer.MAX_VALUE);
		int y = set.getInt("y", Integer.MAX_VALUE);
		int z = set.getInt("z", Integer.MAX_VALUE);
		boolean xDefined = x != Integer.MAX_VALUE;
		boolean yDefined = y != Integer.MAX_VALUE;
		boolean zDefined = z != Integer.MAX_VALUE;
		if (xDefined && yDefined && zDefined)
		{
			this._locations = new ArrayList<>();
			this._locations.add(new ChanceLocation(x, y, z, set.getInt("heading", 0), 100.0));
		}
		else
		{
			if (xDefined || yDefined || zDefined)
			{
				throw new IllegalStateException(String.format("Spawn with partially declared and x: %s y: %s z: %s!", this.processParam(x), this.processParam(y), this.processParam(z)));
			}

			String zoneName = set.getString("zone", null);
			if (zoneName != null)
			{
				SpawnTerritory zone = ZoneManager.getInstance().getSpawnTerritory(zoneName);
				if (zone == null)
				{
					throw new NullPointerException("Spawn with non existing zone requested " + zoneName);
				}

				this._zone = zone;
			}
		}

		this.mergeParameters(spawnTemplate, group);
	}

	private StatSet mergeParameters(SpawnTemplate spawnTemplate, SpawnGroup group)
	{
		if (this._parameters == null && spawnTemplate.getParameters() == null && group.getParameters() == null)
		{
			return null;
		}
		StatSet set = new StatSet();
		if (spawnTemplate.getParameters() != null)
		{
			set.merge(spawnTemplate.getParameters());
		}

		if (group.getParameters() != null)
		{
			set.merge(group.getParameters());
		}

		if (this._parameters != null)
		{
			set.merge(this._parameters);
		}

		return set;
	}

	public void addSpawnLocation(ChanceLocation loc)
	{
		if (this._locations == null)
		{
			this._locations = new ArrayList<>();
		}

		this._locations.add(loc);
	}

	public SpawnTemplate getSpawnTemplate()
	{
		return this._spawnTemplate;
	}

	public SpawnGroup getGroup()
	{
		return this._group;
	}

	protected String processParam(int value)
	{
		return value != Integer.MAX_VALUE ? Integer.toString(value) : "undefined";
	}

	public int getId()
	{
		return this._id;
	}

	public int getCount()
	{
		return this._count;
	}

	public Duration getRespawnTime()
	{
		return this._respawnTime;
	}

	public Duration getRespawnTimeRandom()
	{
		return this._respawnTimeRandom;
	}

	public SchedulingPattern getRespawnPattern()
	{
		return this._respawnPattern;
	}

	public int getChaseRange()
	{
		return this._chaseRange;
	}

	public List<ChanceLocation> getLocation()
	{
		return this._locations;
	}

	public SpawnTerritory getZone()
	{
		return this._zone;
	}

	@Override
	public StatSet getParameters()
	{
		return this._parameters;
	}

	@Override
	public void setParameters(StatSet parameters)
	{
		if (this._parameters == null)
		{
			this._parameters = parameters;
		}
		else
		{
			this._parameters.merge(parameters);
		}
	}

	public boolean hasSpawnAnimation()
	{
		return this._spawnAnimation;
	}

	public boolean hasDBSave()
	{
		return this._saveInDB;
	}

	public String getDBName()
	{
		return this._dbName;
	}

	public List<MinionHolder> getMinions()
	{
		return this._minions != null ? this._minions : Collections.emptyList();
	}

	public void addMinion(MinionHolder minion)
	{
		if (this._minions == null)
		{
			this._minions = new ArrayList<>();
		}

		this._minions.add(minion);
	}

	public Set<Npc> getSpawnedNpcs()
	{
		return this._spawnedNpcs;
	}

	public Location getSpawnLocation()
	{
		if (this._locations != null)
		{
			double locRandom = 100.0 * Rnd.nextDouble();
			float cumulativeChance = 0.0F;

			for (ChanceLocation loc : this._locations)
			{
				if (locRandom <= (cumulativeChance = (float) (cumulativeChance + loc.getChance())))
				{
					return loc;
				}
			}

			LOGGER.warning("Couldn't match location by chance turning first...");
			return null;
		}
		else if (this._zone != null)
		{
			int count = 0;
			Location centerPoint = this._zone.getCenterPoint();
			int centerX = centerPoint.getX();
			int centerY = centerPoint.getY();
			int centerZ = centerPoint.getZ();

			while (count++ < 100)
			{
				Location location = this._zone.getRandomPoint();
				int randomX = location.getX();
				int randomY = location.getY();
				int randomZ = location.getZ();
				if (GeoEngine.getInstance().getHeight(randomX, randomY, randomZ) <= this._zone.getHighZ() && GeoEngine.getInstance().canSeeTarget(randomX, randomY, randomZ, centerX, centerY, centerZ, null))
				{
					location.setHeading(-1);
					return location;
				}
			}

			Location location = this._zone.getRandomPoint();
			location.setHeading(-1);
			return location;
		}
		else if (!this._group.getTerritories().isEmpty())
		{
			SpawnTerritory territory = this._group.getTerritories().get(Rnd.get(this._group.getTerritories().size()));
			int count = 0;
			Location centerPoint = territory.getCenterPoint();
			int centerX = centerPoint.getX();
			int centerY = centerPoint.getY();
			int centerZ = centerPoint.getZ();
			List<BannedSpawnTerritory> bannedTerritories = this._group.getBannedTerritories();
			if (!bannedTerritories.isEmpty())
			{
				label132:
				while (count++ < 100)
				{
					Location location = territory.getRandomPoint();
					int randomX = location.getX();
					int randomY = location.getY();
					int randomZ = location.getZ();

					for (BannedSpawnTerritory banned : bannedTerritories)
					{
						if (banned.isInsideZone(randomX, randomY, randomZ))
						{
							continue label132;
						}
					}

					if (GeoEngine.getInstance().getHeight(randomX, randomY, randomZ) <= territory.getHighZ() && GeoEngine.getInstance().canSeeTarget(randomX, randomY, randomZ, centerX, centerY, centerZ, null))
					{
						location.setHeading(-1);
						return location;
					}
				}

				count = 0;

				label118:
				while (count++ < 100)
				{
					Location location = territory.getRandomPoint();
					int randomX = location.getX();
					int randomY = location.getY();
					int randomZ = location.getZ();

					for (BannedSpawnTerritory bannedx : bannedTerritories)
					{
						if (bannedx.isInsideZone(randomX, randomY, randomZ))
						{
							continue label118;
						}
					}

					location.setHeading(-1);
					return location;
				}
			}

			count = 0;

			while (count++ < 100)
			{
				Location location = territory.getRandomPoint();
				int randomX = location.getX();
				int randomY = location.getY();
				int randomZ = location.getZ();
				if (GeoEngine.getInstance().getHeight(randomX, randomY, randomZ) <= territory.getHighZ() && GeoEngine.getInstance().canSeeTarget(randomX, randomY, randomZ, centerX, centerY, centerZ, null))
				{
					location.setHeading(-1);
					return location;
				}
			}

			Location location = territory.getRandomPoint();
			location.setHeading(-1);
			return location;
		}
		else if (this._spawnTemplate.getTerritories().isEmpty())
		{
			return null;
		}
		else
		{
			SpawnTerritory territory = this._spawnTemplate.getTerritories().get(Rnd.get(this._spawnTemplate.getTerritories().size()));
			int count = 0;
			Location centerPoint = territory.getCenterPoint();
			int centerX = centerPoint.getX();
			int centerY = centerPoint.getY();
			int centerZ = centerPoint.getZ();
			List<BannedSpawnTerritory> bannedTerritories = this._spawnTemplate.getBannedTerritories();
			if (!bannedTerritories.isEmpty())
			{
				label157:
				while (count++ < 100)
				{
					Location location = territory.getRandomPoint();
					int randomX = location.getX();
					int randomY = location.getY();
					int randomZ = location.getZ();

					for (BannedSpawnTerritory bannedxx : bannedTerritories)
					{
						if (bannedxx.isInsideZone(randomX, randomY, randomZ))
						{
							continue label157;
						}
					}

					if (GeoEngine.getInstance().getHeight(randomX, randomY, randomZ) <= territory.getHighZ() && GeoEngine.getInstance().canSeeTarget(randomX, randomY, randomZ, centerX, centerY, centerZ, null))
					{
						location.setHeading(-1);
						return location;
					}
				}

				count = 0;

				label143:
				while (count++ < 100)
				{
					Location location = territory.getRandomPoint();
					int randomX = location.getX();
					int randomY = location.getY();
					int randomZ = location.getZ();

					for (BannedSpawnTerritory bannedxxx : bannedTerritories)
					{
						if (bannedxxx.isInsideZone(randomX, randomY, randomZ))
						{
							continue label143;
						}
					}

					location.setHeading(-1);
					return location;
				}
			}

			count = 0;

			while (count++ < 100)
			{
				Location location = territory.getRandomPoint();
				int randomX = location.getX();
				int randomY = location.getY();
				int randomZ = location.getZ();
				if (GeoEngine.getInstance().getHeight(randomX, randomY, randomZ) <= territory.getHighZ() && GeoEngine.getInstance().canSeeTarget(randomX, randomY, randomZ, centerX, centerY, centerZ, null))
				{
					location.setHeading(-1);
					return location;
				}
			}

			Location location = territory.getRandomPoint();
			location.setHeading(-1);
			return location;
		}
	}

	public void spawn()
	{
		this.spawn(null);
	}

	public void spawn(Instance instance)
	{
		try
		{
			NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(this._id);
			if (npcTemplate == null)
			{
				LOGGER.warning("Attempting to spawn unexisting npc id: " + this._id + " file: " + this._spawnTemplate.getFile().getName() + " spawn: " + this._spawnTemplate.getName() + " group: " + this._group.getName());
				return;
			}

			if (npcTemplate.isType("Defender"))
			{
				LOGGER.warning("Attempting to spawn npc id: " + this._id + " type: " + npcTemplate.getType() + " file: " + this._spawnTemplate.getFile().getName() + " spawn: " + this._spawnTemplate.getName() + " group: " + this._group.getName());
				return;
			}

			for (int i = 0; i < this._count; i++)
			{
				this.spawnNpc(npcTemplate, instance);
			}
		}
		catch (Exception var4)
		{
			LOGGER.warning("Couldn't spawn npc " + this._id + var4);
		}
	}

	private void spawnNpc(NpcTemplate npcTemplate, Instance instance) throws ClassNotFoundException, NoSuchMethodException, ClassCastException
	{
		Spawn spawn = new Spawn(npcTemplate);
		Location loc = this.getSpawnLocation();
		if (loc == null)
		{
			LOGGER.warning("Couldn't initialize new spawn, no location found!");
		}
		else
		{
			spawn.setInstanceId(instance != null ? instance.getId() : 0);
			spawn.setAmount(1);
			spawn.setXYZ(loc);
			spawn.setHeading(loc.getHeading());
			spawn.setLocation(loc);
			int respawn = 0;
			int respawnRandom = 0;
			SchedulingPattern respawnPattern = null;
			if (this._respawnTime != null)
			{
				respawn = (int) this._respawnTime.getSeconds();
			}

			if (this._respawnTimeRandom != null)
			{
				respawnRandom = (int) this._respawnTimeRandom.getSeconds();
			}

			if (this._respawnPattern != null)
			{
				respawnPattern = this._respawnPattern;
			}

			if (respawn <= 0 && respawnPattern == null)
			{
				spawn.stopRespawn();
			}
			else
			{
				spawn.setRespawnDelay(respawn, respawnRandom);
				spawn.setRespawnPattern(respawnPattern);
				spawn.startRespawn();
			}

			spawn.setSpawnTemplate(this);
			if (this._saveInDB)
			{
				if (!DatabaseSpawnManager.getInstance().isDefined(this._id))
				{
					Npc spawnedNpc = DatabaseSpawnManager.getInstance().addNewSpawn(spawn, true);
					if (spawnedNpc != null && spawnedNpc.isMonster() && this._minions != null)
					{
						spawnedNpc.asMonster().getMinionList().spawnMinions(this._minions);
					}

					this._spawnedNpcs.add(spawnedNpc);
				}
			}
			else
			{
				Npc npc = spawn.doSpawn(this._spawnAnimation);
				if (npc.isMonster() && this._minions != null)
				{
					npc.asMonster().getMinionList().spawnMinions(this._minions);
				}

				this._spawnedNpcs.add(npc);
				SpawnTable.getInstance().addSpawn(spawn);
			}
		}
	}

	public void despawn()
	{
		this._spawnedNpcs.forEach(npc -> {
			npc.getSpawn().stopRespawn();
			SpawnTable.getInstance().removeSpawn(npc.getSpawn());
			npc.deleteMe();
		});
		this._spawnedNpcs.clear();
	}

	public void notifySpawnNpc(Npc npc)
	{
		this._spawnTemplate.notifyEvent(event -> event.onSpawnNpc(this._spawnTemplate, this._group, npc));
	}

	public void notifyDespawnNpc(Npc npc)
	{
		this._spawnTemplate.notifyEvent(event -> event.onSpawnDespawnNpc(this._spawnTemplate, this._group, npc));
	}

	public void notifyNpcDeath(Npc npc, Creature killer)
	{
		this._spawnTemplate.notifyEvent(event -> event.onSpawnNpcDeath(this._spawnTemplate, this._group, npc, killer));
	}

	public int getHighZ()
	{
		return this._zone != null ? this._zone.getHighZ() : Integer.MAX_VALUE;
	}

	@Override
	public NpcSpawnTemplate clone()
	{
		return new NpcSpawnTemplate(this);
	}
}
