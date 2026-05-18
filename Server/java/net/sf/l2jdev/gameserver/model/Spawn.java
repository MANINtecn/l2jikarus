package net.sf.l2jdev.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.time.SchedulingPattern;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.custom.RandomSpawnsConfig;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.model.zone.type.WaterZone;
import net.sf.l2jdev.gameserver.taskmanagers.RespawnTaskManager;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class Spawn extends Location
{
	protected static final Logger LOGGER = Logger.getLogger(Spawn.class.getName());
	private String _name;
	private NpcTemplate _template;
	private int _maximumCount;
	private int _currentCount;
	public int _scheduledCount;
	private int _locationId;
	private int _instanceId = 0;
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	private SchedulingPattern _respawnPattern;
	private Constructor<? extends Npc> _constructor;
	private boolean _doRespawn = true;
	private final Deque<Npc> _spawnedNpcs = new ConcurrentLinkedDeque<>();
	private boolean _randomWalk = false;
	private NpcSpawnTemplate _spawnTemplate;

	public Spawn(NpcTemplate template) throws ClassNotFoundException, NoSuchMethodException, ClassCastException
	{
		super(0, 0, -10000);
		this._template = template;
		if (this._template != null)
		{
			String className = "net.sf.l2jdev.gameserver.model.actor.instance." + this._template.getType();
			this._constructor = Class.forName(className).asSubclass(Npc.class).getConstructor(NpcTemplate.class);
		}
	}

	public Spawn(int npcId) throws ClassNotFoundException, NoSuchMethodException, ClassCastException
	{
		super(0, 0, -10000);
		this._template = Objects.requireNonNull(NpcData.getInstance().getTemplate(npcId), "NpcTemplate not found for NPC ID: " + npcId);
		String className = "net.sf.l2jdev.gameserver.model.actor.instance." + this._template.getType();
		this._constructor = Class.forName(className).asSubclass(Npc.class).getConstructor(NpcTemplate.class);
	}

	public int getAmount()
	{
		return this._maximumCount;
	}

	public String getName()
	{
		return this._name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public int getLocationId()
	{
		return this._locationId;
	}

	public int getId()
	{
		return this._template.getId();
	}

	public int getRespawnMinDelay()
	{
		return this._respawnMinDelay;
	}

	public int getRespawnMaxDelay()
	{
		return this._respawnMaxDelay;
	}

	public SchedulingPattern getRespawnPattern()
	{
		return this._respawnPattern;
	}

	public void setAmount(int amount)
	{
		this._maximumCount = amount;
	}

	public void setLocationId(int id)
	{
		this._locationId = id;
	}

	public void setRespawnMinDelay(int date)
	{
		this._respawnMinDelay = date;
	}

	public void setRespawnMaxDelay(int date)
	{
		this._respawnMaxDelay = date;
	}

	public void decreaseCount(Npc oldNpc)
	{
		if (this._currentCount > 0)
		{
			this._currentCount--;
			this._spawnedNpcs.remove(oldNpc);
			if (this._doRespawn && this._scheduledCount + this._currentCount < this._maximumCount)
			{
				this._scheduledCount++;
				RespawnTaskManager.getInstance().add(oldNpc, System.currentTimeMillis() + (this.hasRespawnRandom() ? Rnd.get(this._respawnMinDelay, this._respawnMaxDelay) : this._respawnMinDelay));
			}
		}
	}

	public int init()
	{
		while (this._currentCount < this._maximumCount)
		{
			this.doSpawn();
		}

		this._doRespawn = this._respawnMinDelay > 0;
		return this._currentCount;
	}

	public boolean isRespawnEnabled()
	{
		return this._doRespawn;
	}

	public void stopRespawn()
	{
		this._doRespawn = false;
	}

	public void startRespawn()
	{
		this._doRespawn = true;
	}

	public Npc doSpawn()
	{
		return this._doRespawn ? this.doSpawn(false) : null;
	}

	public Npc doSpawn(boolean isSummonSpawn)
	{
		try
		{
			if (!this._template.isType("Pet") && !this._template.isType("Decoy") && !this._template.isType("Trap"))
			{
				Npc npc = this._constructor.newInstance(this._template);
				npc.setInstanceById(this._instanceId);
				if (isSummonSpawn)
				{
					npc.setShowSummonAnimation(isSummonSpawn);
				}

				return this.initializeNpc(npc);
			}
			this._currentCount++;
			return null;
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.WARNING, "Error while spawning " + this._template.getId(), var3);
			return null;
		}
	}

	private Npc initializeNpc(Npc npc)
	{
		npc.onRespawn();
		int newlocx = 0;
		int newlocy = 0;
		int newlocz = -10000;
		if (this._spawnTemplate != null)
		{
			Location loc = this._spawnTemplate.getSpawnLocation();
			newlocx = loc.getX();
			newlocy = loc.getY();
			newlocz = loc.getZ();
			this.setLocation(loc);
		}
		else
		{
			if (this.getX() == 0 && this.getY() == 0)
			{
				LOGGER.warning("NPC " + npc + " doesn't have spawn location!");
				return null;
			}

			newlocx = this.getX();
			newlocy = this.getY();
			newlocz = this.getZ();
		}

		WaterZone water = ZoneManager.getInstance().getZone(newlocx, newlocy, newlocz, WaterZone.class);
		if (RandomSpawnsConfig.ENABLE_RANDOM_MONSTER_SPAWNS && this.getHeading() != -1 && npc.isMonster() && !npc.isQuestMonster() && !WalkingManager.getInstance().isTargeted(npc) && this.getInstanceId() == 0 && !this.getTemplate().isUndying() && !npc.isRaid() && !npc.isRaidMinion() && !npc.isFlying() && water == null && !RandomSpawnsConfig.MOBS_LIST_NOT_RANDOM.contains(npc.getId()))
		{
			int randX = newlocx + Rnd.get(RandomSpawnsConfig.MOB_MIN_SPAWN_RANGE, RandomSpawnsConfig.MOB_MAX_SPAWN_RANGE);
			int randY = newlocy + Rnd.get(RandomSpawnsConfig.MOB_MIN_SPAWN_RANGE, RandomSpawnsConfig.MOB_MAX_SPAWN_RANGE);
			if (GeoEngine.getInstance().canMoveToTarget(newlocx, newlocy, newlocz, randX, randY, newlocz, npc.getInstanceWorld()) && GeoEngine.getInstance().canSeeTarget(newlocx, newlocy, newlocz, randX, randY, newlocz, npc.getInstanceWorld()))
			{
				newlocx = randX;
				newlocy = randY;
				this.setXYZ(randX, randY, newlocz);
				this.setHeading(-1);
			}
		}

		if (npc.isMonster() && !npc.isFlying() && water == null)
		{
			int geoZ = GeoEngine.getInstance().getHeight(newlocx, newlocy, newlocz);
			if (LocationUtil.calculateDistance(newlocx, newlocy, newlocz, newlocx, newlocy, geoZ, true, true) < 300.0)
			{
				newlocz = geoZ;
			}

			if (this._spawnTemplate != null)
			{
				int highZ = this._spawnTemplate.getHighZ();
				if (highZ != Integer.MAX_VALUE && highZ < newlocz)
				{
					newlocz = highZ;
				}
			}
		}

		npc.setRandomWalking(this._randomWalk);
		if (this.getHeading() == -1)
		{
			npc.setHeading(Rnd.get(61794));
		}
		else
		{
			npc.setHeading(this.getHeading());
		}

		if (npc.getTemplate().isUsingServerSideName())
		{
			npc.setName(npc.getTemplate().getName());
		}

		if (npc.getTemplate().isUsingServerSideTitle())
		{
			npc.setTitle(npc.getTemplate().getTitle());
		}

		npc.setSpawn(this);
		npc.spawnMe(newlocx, newlocy, newlocz);
		if (npc.getInstanceId() > 0)
		{
			npc.broadcastInfo();
		}

		if (this._spawnTemplate != null)
		{
			this._spawnTemplate.notifySpawnNpc(npc);
		}

		this._spawnedNpcs.add(npc);
		this._currentCount++;
		if (npc.isMonster() && NpcData.getMasterMonsterIDs().contains(npc.getId()))
		{
			npc.asMonster().getMinionList().spawnMinions(npc.getParameters().getMinionList("Privates"));
		}

		return npc;
	}

	public void setRespawnDelay(int delay, int randomInterval)
	{
		if (delay != 0)
		{
			if (delay < 0)
			{
				LOGGER.warning("respawn delay is negative for spawn:" + this);
			}

			int minDelay = delay - randomInterval;
			int maxDelay = delay + randomInterval;
			this._respawnMinDelay = Math.max(1, minDelay) * 1000;
			this._respawnMaxDelay = Math.max(1, maxDelay) * 1000;
		}
		else
		{
			this._respawnMinDelay = 0;
			this._respawnMaxDelay = 0;
		}
	}

	public void setRespawnPattern(SchedulingPattern respawnPattern)
	{
		this._respawnPattern = respawnPattern;
	}

	public void setRespawnDelay(int delay)
	{
		this.setRespawnDelay(delay, 0);
	}

	public int getRespawnDelay()
	{
		return (this._respawnMinDelay + this._respawnMaxDelay) / 2;
	}

	public boolean hasRespawnRandom()
	{
		return this._respawnMinDelay != this._respawnMaxDelay;
	}

	public int getChaseRange()
	{
		return this._spawnTemplate == null ? 0 : this._spawnTemplate.getChaseRange();
	}

	public Npc getLastSpawn()
	{
		return !this._spawnedNpcs.isEmpty() ? this._spawnedNpcs.peekLast() : null;
	}

	public boolean deleteLastNpc()
	{
		return !this._spawnedNpcs.isEmpty() && this._spawnedNpcs.getLast().deleteMe();
	}

	public Deque<Npc> getSpawnedNpcs()
	{
		return this._spawnedNpcs;
	}

	public void respawnNpc(Npc oldNpc)
	{
		if (this._doRespawn)
		{
			this.initializeNpc(oldNpc);
			Instance instance = oldNpc.getInstanceWorld();
			if (instance != null)
			{
				instance.addNpc(oldNpc);
			}
		}
	}

	public NpcTemplate getTemplate()
	{
		return this._template;
	}

	public int getInstanceId()
	{
		return this._instanceId;
	}

	public void setInstanceId(int instanceId)
	{
		this._instanceId = instanceId;
	}

	public boolean getRandomWalking()
	{
		return this._randomWalk;
	}

	public void setRandomWalking(boolean value)
	{
		this._randomWalk = value;
	}

	public void setSpawnTemplate(NpcSpawnTemplate npcSpawnTemplate)
	{
		this._spawnTemplate = npcSpawnTemplate;
	}

	public NpcSpawnTemplate getNpcSpawnTemplate()
	{
		return this._spawnTemplate;
	}

	@Override
	public String toString()
	{
		return "Spawn ID: " + this._template.getId() + " X: " + this.getX() + " Y: " + this.getY() + " Z: " + this.getZ() + " Heading: " + this.getHeading();
	}
}
