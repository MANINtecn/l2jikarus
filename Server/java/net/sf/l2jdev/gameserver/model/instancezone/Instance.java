package net.sf.l2jdev.gameserver.model.instancezone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.actor.templates.DoorTemplate;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceCreated;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceEnter;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceLeave;
import net.sf.l2jdev.gameserver.model.events.holders.instance.OnInstanceStatusChange;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.model.spawns.SpawnGroup;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.ArrayUtil;

public class Instance
{
	private static final Logger LOGGER = Logger.getLogger(Instance.class.getName());
	private final int _id;
	private final InstanceTemplate _template;
	private final long _startTime;
	private long _endTime;
	private final Set<Integer> _allowed = ConcurrentHashMap.newKeySet();
	private final Set<Player> _players = ConcurrentHashMap.newKeySet();
	private final Set<Npc> _npcs = ConcurrentHashMap.newKeySet();
	private final Map<Integer, Door> _doors = new HashMap<>();
	private final StatSet _parameters = new StatSet();
	private final Map<Integer, ScheduledFuture<?>> _ejectDeadTasks = new ConcurrentHashMap<>();
	private ScheduledFuture<?> _cleanUpTask = null;
	private ScheduledFuture<?> _emptyDestroyTask = null;
	private final List<SpawnTemplate> _spawns;

	public Instance(int id, InstanceTemplate template, Player player)
	{
		this._id = id;
		this._template = template;
		this._startTime = System.currentTimeMillis();
		this._spawns = new ArrayList<>(template.getSpawns().size());

		for (SpawnTemplate spawn : template.getSpawns())
		{
			this._spawns.add(spawn.clone());
		}

		InstanceManager.getInstance().register(this);
		this.setDuration(this._template.getDuration());
		this.setStatus(0);
		this.spawnDoors();

		for (SpawnTemplate spawnTemplate : this._spawns)
		{
			if (spawnTemplate.isSpawningByDefault())
			{
				spawnTemplate.spawnAll(this);
			}
		}

		if (!this.isDynamic() && EventDispatcher.getInstance().hasListener(EventType.ON_INSTANCE_CREATED, this._template))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnInstanceCreated(this, player), this._template);
		}
	}

	public int getId()
	{
		return this._id;
	}

	public String getName()
	{
		return this._template.getName();
	}

	public boolean isDynamic()
	{
		return this._template.getId() == -1;
	}

	public void setParameter(String key, Object value)
	{
		if (value == null)
		{
			this._parameters.remove(key);
		}
		else
		{
			this._parameters.set(key, value);
		}
	}

	public void setParameter(String key, boolean value)
	{
		this._parameters.set(key, value ? Boolean.TRUE : Boolean.FALSE);
	}

	public StatSet getParameters()
	{
		return this._parameters;
	}

	public int getStatus()
	{
		return this._parameters.getInt("INSTANCE_STATUS", 0);
	}

	public boolean isStatus(int status)
	{
		return this.getStatus() == status;
	}

	public void setStatus(int value)
	{
		this._parameters.set("INSTANCE_STATUS", value);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_INSTANCE_STATUS_CHANGE, this._template))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnInstanceStatusChange(this, value), this._template);
		}
	}

	public int incStatus()
	{
		int status = this.getStatus() + 1;
		this.setStatus(status);
		return status;
	}

	public void addAllowed(Player player)
	{
		if (!this._allowed.contains(player.getObjectId()))
		{
			this._allowed.add(player.getObjectId());
		}
	}

	public boolean isAllowed(Player player)
	{
		return this._allowed.contains(player.getObjectId());
	}

	public List<Player> getAllowed()
	{
		List<Player> allowed = new ArrayList<>(this._allowed.size());

		for (int playerId : this._allowed)
		{
			Player player = World.getInstance().getPlayer(playerId);
			if (player != null)
			{
				allowed.add(player);
			}
		}

		return allowed;
	}

	public void addPlayer(Player player)
	{
		this._players.add(player);
		if (this._emptyDestroyTask != null)
		{
			this._emptyDestroyTask.cancel(false);
			this._emptyDestroyTask = null;
		}
	}

	public void removePlayer(Player player)
	{
		this._players.remove(player);
		if (this._players.isEmpty())
		{
			long emptyTime = this._template.getEmptyDestroyTime();
			if (this._template.getDuration() == 0 || emptyTime == 0L)
			{
				this.destroy();
			}
			else if (emptyTime >= 0L && this._emptyDestroyTask == null)
			{
				this._emptyDestroyTask = ThreadPool.schedule(this::destroy, emptyTime);
			}
		}
	}

	public boolean containsPlayer(Player player)
	{
		return this._players.contains(player);
	}

	public Set<Player> getPlayers()
	{
		return this._players;
	}

	public int getPlayersCount()
	{
		return this._players.size();
	}

	public Player getFirstPlayer()
	{
		Iterator<Player> var1 = this._players.iterator();
		return var1.hasNext() ? (Player) var1.next() : null;
	}

	public Player getPlayerById(int id)
	{
		for (Player player : this._players)
		{
			if (player.getObjectId() == id)
			{
				return player;
			}
		}

		return null;
	}

	public List<Player> getPlayersInsideRadius(ILocational object, int radius)
	{
		List<Player> result = new LinkedList<>();

		for (Player player : this._players)
		{
			if (player.isInsideRadius3D(object, radius))
			{
				result.add(player);
			}
		}

		return result;
	}

	private void spawnDoors()
	{
		for (DoorTemplate template : this._template.getDoors().values())
		{
			this._doors.put(template.getId(), DoorData.getInstance().spawnDoor(template, this));
		}
	}

	public Collection<Door> getDoors()
	{
		return this._doors.values();
	}

	public Door getDoor(int id)
	{
		return this._doors.get(id);
	}

	public void openCloseDoor(int id, boolean open)
	{
		Door door = this._doors.get(id);
		if (door != null)
		{
			if (open)
			{
				if (!door.isOpen())
				{
					door.openMe();
				}
			}
			else if (door.isOpen())
			{
				door.closeMe();
			}
		}
	}

	public boolean isSpawnGroupExist(String name)
	{
		for (SpawnTemplate spawnTemplate : this._spawns)
		{
			for (SpawnGroup group : spawnTemplate.getGroups())
			{
				if (name.equalsIgnoreCase(group.getName()))
				{
					return true;
				}
			}
		}

		return false;
	}

	public List<SpawnGroup> getSpawnGroup(String name)
	{
		List<SpawnGroup> spawns = new LinkedList<>();

		for (SpawnTemplate spawnTemplate : this._spawns)
		{
			spawns.addAll(spawnTemplate.getGroupsByName(name));
		}

		return spawns;
	}

	public List<Npc> getNpcsOfGroup(String name)
	{
		return this.getNpcsOfGroup(name, null);
	}

	public List<Npc> getNpcsOfGroup(String groupName, Predicate<Npc> filterValue)
	{
		Predicate<Npc> filter = filterValue;
		if (filterValue == null)
		{
			filter = Objects::nonNull;
		}

		List<Npc> npcs = new LinkedList<>();

		for (SpawnTemplate spawnTemplate : this._spawns)
		{
			for (SpawnGroup group : spawnTemplate.getGroupsByName(groupName))
			{
				for (NpcSpawnTemplate npcTemplate : group.getSpawns())
				{
					for (Npc npc : npcTemplate.getSpawnedNpcs())
					{
						if (filter.test(npc))
						{
							npcs.add(npc);
						}
					}
				}
			}
		}

		return npcs;
	}

	public Npc getNpcOfGroup(String groupName, Predicate<Npc> filterValue)
	{
		Predicate<Npc> filter = filterValue;
		if (filterValue == null)
		{
			filter = Objects::nonNull;
		}

		for (SpawnTemplate spawnTemplate : this._spawns)
		{
			for (SpawnGroup group : spawnTemplate.getGroupsByName(groupName))
			{
				for (NpcSpawnTemplate npcTemplate : group.getSpawns())
				{
					for (Npc npc : npcTemplate.getSpawnedNpcs())
					{
						if (filter.test(npc))
						{
							return npc;
						}
					}
				}
			}
		}

		return null;
	}

	public List<Npc> spawnGroup(String name)
	{
		List<SpawnGroup> spawns = this.getSpawnGroup(name);
		if (spawns == null)
		{
			LOGGER.warning("Spawn group " + name + " doesn't exist for instance " + this._template.getName() + " (" + this._id + ")!");
			return Collections.emptyList();
		}
		List<Npc> npcs = new LinkedList<>();

		try
		{
			for (SpawnGroup holder : spawns)
			{
				holder.spawnAll(this);
				holder.getSpawns().forEach(spawn -> npcs.addAll(spawn.getSpawnedNpcs()));
			}
		}
		catch (Exception var6)
		{
			LOGGER.warning("Unable to spawn group " + name + " inside instance " + this._template.getName() + " (" + this._id + ")");
		}

		return npcs;
	}

	public void despawnGroup(String name)
	{
		List<SpawnGroup> spawns = this.getSpawnGroup(name);
		if (spawns == null)
		{
			LOGGER.warning("Spawn group " + name + " doesn't exist for instance " + this._template.getName() + " (" + this._id + ")!");
		}
		else
		{
			try
			{
				spawns.forEach(SpawnGroup::despawnAll);
			}
			catch (Exception var4)
			{
				LOGGER.warning("Unable to spawn group " + name + " inside instance " + this._template.getName() + " (" + this._id + ")");
			}
		}
	}

	public Set<Npc> getNpcs()
	{
		return this._npcs;
	}

	public List<Npc> getNpcs(int... id)
	{
		List<Npc> result = new LinkedList<>();

		for (Npc npc : this._npcs)
		{
			if (ArrayUtil.contains(id, npc.getId()))
			{
				result.add(npc);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final <T extends Creature> List<T> getNpcs(Class<T> clazz, int... ids)
	{
		List<T> result = new LinkedList<>();

		for (Npc npc : this._npcs)
		{
			if ((ids.length == 0 || ArrayUtil.contains(ids, npc.getId())) && clazz.isInstance(npc))
			{
				result.add((T) npc);
			}
		}

		return result;
	}

	public List<Npc> getAliveNpcs()
	{
		List<Npc> result = new LinkedList<>();

		for (Npc npc : this._npcs)
		{
			if (npc.getCurrentHp() > 0.0)
			{
				result.add(npc);
			}
		}

		return result;
	}

	public List<Npc> getAliveNpcs(int... id)
	{
		List<Npc> result = new LinkedList<>();

		for (Npc npc : this._npcs)
		{
			if (npc.getCurrentHp() > 0.0 && ArrayUtil.contains(id, npc.getId()))
			{
				result.add(npc);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final <T extends Creature> List<T> getAliveNpcs(Class<T> clazz, int... ids)
	{
		List<T> result = new LinkedList<>();

		for (Npc npc : this._npcs)
		{
			if ((ids.length == 0 || ArrayUtil.contains(ids, npc.getId())) && npc.getCurrentHp() > 0.0 && clazz.isInstance(npc))
			{
				result.add((T) npc);
			}
		}

		return result;
	}

	public int getAliveNpcCount()
	{
		int count = 0;

		for (Npc npc : this._npcs)
		{
			if (npc.getCurrentHp() > 0.0)
			{
				count++;
			}
		}

		return count;
	}

	public int getAliveNpcCount(int... id)
	{
		int count = 0;

		for (Npc npc : this._npcs)
		{
			if (npc.getCurrentHp() > 0.0 && ArrayUtil.contains(id, npc.getId()))
			{
				count++;
			}
		}

		return count;
	}

	public Npc getNpc(int id)
	{
		for (Npc npc : this._npcs)
		{
			if (npc.getId() == id)
			{
				return npc;
			}
		}

		return null;
	}

	public void addNpc(Npc npc)
	{
		this._npcs.add(npc);
	}

	public void removeNpc(Npc npc)
	{
		this._npcs.remove(npc);
	}

	private void removePlayers()
	{
		this._players.forEach(this::ejectPlayer);
		this._players.clear();
	}

	private void removeDoors()
	{
		for (Door door : this._doors.values())
		{
			if (door != null)
			{
				door.decayMe();
			}
		}

		this._doors.clear();
	}

	public void removeNpcs()
	{
		this._spawns.forEach(SpawnTemplate::despawnAll);
		this._npcs.forEach(Npc::deleteMe);
		this._npcs.clear();
	}

	public void setDuration(int minutes)
	{
		if (minutes < 0)
		{
			this._endTime = -1L;
		}
		else
		{
			long millis = TimeUnit.MINUTES.toMillis(minutes);
			if (this._cleanUpTask != null)
			{
				this._cleanUpTask.cancel(true);
				this._cleanUpTask = null;
			}

			if (this._emptyDestroyTask != null && millis < this._emptyDestroyTask.getDelay(TimeUnit.MILLISECONDS))
			{
				this._emptyDestroyTask.cancel(true);
				this._emptyDestroyTask = null;
			}

			this._endTime = System.currentTimeMillis() + millis;
			if (minutes < 1)
			{
				this.destroy();
			}
			else
			{
				this.sendWorldDestroyMessage(minutes);
				if (minutes <= 5)
				{
					this._cleanUpTask = ThreadPool.schedule(this::cleanUp, millis - 60000L);
				}
				else
				{
					this._cleanUpTask = ThreadPool.schedule(this::cleanUp, millis - 300000L);
				}
			}
		}
	}

	public synchronized void destroy()
	{
		if (this._cleanUpTask != null)
		{
			this._cleanUpTask.cancel(false);
			this._cleanUpTask = null;
		}

		if (this._emptyDestroyTask != null)
		{
			this._emptyDestroyTask.cancel(false);
			this._emptyDestroyTask = null;
		}

		this._ejectDeadTasks.values().forEach(t -> t.cancel(true));
		this._ejectDeadTasks.clear();
		if (!this.isDynamic() && EventDispatcher.getInstance().hasListener(EventType.ON_INSTANCE_DESTROY, this._template))
		{
			EventDispatcher.getInstance().notifyEvent(new OnInstanceDestroy(this), this._template);
		}

		if (this._template.getEmptyDestroyTime() > 0L && this._template.getReenterType() == InstanceReenterType.ON_FINISH)
		{
			this.setReenterTime();
		}

		this.removePlayers();
		this.removeDoors();
		this.removeNpcs();
		InstanceManager.getInstance().unregister(this.getId());
	}

	public void ejectPlayer(Player player)
	{
		Instance world = player.getInstanceWorld();
		if (world != null && world.equals(this))
		{
			Location loc = this._template.getExitLocation(player);
			if (loc != null)
			{
				player.teleToLocation(loc, null);
			}
			else
			{
				player.teleToLocation(TeleportWhereType.TOWN, null);
			}
		}
	}

	public void broadcastPacket(ServerPacket... packets)
	{
		for (Player player : this._players)
		{
			for (ServerPacket packet : packets)
			{
				player.sendPacket(packet);
			}
		}
	}

	public long getStartTime()
	{
		return this._startTime;
	}

	public long getElapsedTime()
	{
		return System.currentTimeMillis() - this._startTime;
	}

	public long getRemainingTime()
	{
		return this._endTime == -1L ? -1L : this._endTime - System.currentTimeMillis();
	}

	public long getEndTime()
	{
		return this._endTime;
	}

	public void setReenterTime()
	{
		this.setReenterTime(this._template.calculateReenterTime());
	}

	public void setReenterTime(long time)
	{
		if (this._template.getId() != -1 || time <= 0L)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO character_instance_time (charId,instanceId,time) VALUES (?,?,?)");)
			{
				for (Integer playerId : this._allowed)
				{
					ps.setInt(1, playerId);
					ps.setInt(2, this._template.getId());
					ps.setLong(3, time);
					ps.addBatch();
				}

				ps.executeBatch();
				SystemMessage msg = new SystemMessage(SystemMessageId.INSTANCE_ZONE_S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_POSSIBLE_ENTRY_TIME_WITH_INSTANCEZONE);
				if (InstanceManager.getInstance().getInstanceName(this.getTemplateId()) != null)
				{
					msg.addInstanceName(this._template.getId());
				}
				else
				{
					msg.addString(this._template.getName());
				}

				this._allowed.forEach(playerIdx -> {
					InstanceManager.getInstance().setReenterPenalty(playerIdx, this.getTemplateId(), time);
					Player player = World.getInstance().getPlayer(playerIdx);
					if (player != null && player.isOnline())
					{
						player.sendPacket(msg);
					}
				});
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Could not insert character instance reenter data: ", var11);
			}
		}
	}

	public void finishInstance()
	{
		this.finishInstance(GeneralConfig.INSTANCE_FINISH_TIME);
	}

	public void finishInstance(int delay)
	{
		if (this._template.getReenterType() == InstanceReenterType.ON_FINISH)
		{
			this.setReenterTime();
		}

		this.setDuration(delay);
	}

	public void onDeath(Player player)
	{
		if (!player.isOnEvent() && this._template.getEjectTime() > 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.IF_YOU_ARE_NOT_RESURRECTED_IN_S1_MIN_YOU_WILL_BE_TELEPORTED_OUT_OF_THE_INSTANCE_ZONE);
			sm.addInt(this._template.getEjectTime());
			player.sendPacket(sm);
			ScheduledFuture<?> oldTAsk = this._ejectDeadTasks.put(player.getObjectId(), ThreadPool.schedule(() -> {
				if (player.isDead())
				{
					this.ejectPlayer(player.asPlayer());
				}
			}, this._template.getEjectTime() * 60 * 1000));
			if (oldTAsk != null)
			{
				oldTAsk.cancel(true);
			}
		}
	}

	public void doRevive(Player player)
	{
		ScheduledFuture<?> task = this._ejectDeadTasks.remove(player.getObjectId());
		if (task != null)
		{
			task.cancel(true);
		}
	}

	public void onInstanceChange(WorldObject object, boolean enter)
	{
		if (object.isPlayer())
		{
			Player player = object.asPlayer();
			if (enter)
			{
				this.addPlayer(player);
				long emptyTime = this._template.getEmptyDestroyTime();
				if (this._emptyDestroyTask != null && emptyTime > 0L && this.getRemainingTime() > emptyTime)
				{
					this._emptyDestroyTask.cancel(false);
					this._emptyDestroyTask = null;
				}

				if (this._template.getExitLocationType() == InstanceTeleportType.ORIGIN)
				{
					player.getVariables().set("INSTANCE_ORIGIN", player.getX() + ";" + player.getY() + ";" + player.getZ());
				}

				if (this._template.isRemoveBuffEnabled())
				{
					this._template.removePlayerBuff(player);
				}

				if (!this.isDynamic() && EventDispatcher.getInstance().hasListener(EventType.ON_INSTANCE_ENTER, this._template))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnInstanceEnter(player, this), this._template);
				}
			}
			else
			{
				this.removePlayer(player);
				if (!this.isDynamic() && EventDispatcher.getInstance().hasListener(EventType.ON_INSTANCE_LEAVE, this._template))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnInstanceLeave(player, this), this._template);
				}
			}
		}
		else if (object.isNpc())
		{
			Npc npc = object.asNpc();
			if (enter)
			{
				this.addNpc(npc);
			}
			else
			{
				if (npc.getSpawn() != null)
				{
					npc.getSpawn().stopRespawn();
				}

				this.removeNpc(npc);
			}
		}
	}

	public void onPlayerLogout(Player player)
	{
		this.removePlayer(player);
		if (GeneralConfig.RESTORE_PLAYER_INSTANCE)
		{
			player.getVariables().set("INSTANCE_RESTORE", this._id);
		}
		else
		{
			Location loc = this.getExitLocation(player);
			if (loc != null)
			{
				player.setLocationInvisible(loc);
				Summon pet = player.getPet();
				if (pet != null)
				{
					pet.teleToLocation(loc, true);
				}
			}
		}
	}

	public StatSet getTemplateParameters()
	{
		return this._template.getParameters();
	}

	public int getTemplateId()
	{
		return this._template.getId();
	}

	public InstanceReenterType getReenterType()
	{
		return this._template.getReenterType();
	}

	public boolean isPvP()
	{
		return this._template.isPvP();
	}

	public boolean isPlayerSummonAllowed()
	{
		return this._template.isPlayerSummonAllowed();
	}

	public Location getEnterLocation()
	{
		return this._template.getEnterLocation();
	}

	public List<Location> getEnterLocations()
	{
		return this._template.getEnterLocations();
	}

	public Location getExitLocation(Player player)
	{
		return this._template.getExitLocation(player);
	}

	public float getExpRate()
	{
		return this._template.getExpRate();
	}

	public float getSPRate()
	{
		return this._template.getSPRate();
	}

	public float getExpPartyRate()
	{
		return this._template.getExpPartyRate();
	}

	public float getSPPartyRate()
	{
		return this._template.getSPPartyRate();
	}

	private void cleanUp()
	{
		if (this._cleanUpTask != null)
		{
			this._cleanUpTask.cancel(true);
		}

		if (this.getRemainingTime() <= TimeUnit.MINUTES.toMillis(1L))
		{
			this.sendWorldDestroyMessage(1);
			this._cleanUpTask = ThreadPool.schedule(this::destroy, 60000L);
		}
		else
		{
			this.sendWorldDestroyMessage(5);
			this._cleanUpTask = ThreadPool.schedule(this::cleanUp, 300000L);
		}
	}

	private void sendWorldDestroyMessage(int delay)
	{
		if (delay <= 5)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THE_INSTANCE_ZONE_EXPIRES_IN_S1_MIN_AFTER_THAT_YOU_WILL_BE_TELEPORTED_OUTSIDE_2);
			sm.addInt(delay);
			this.broadcastPacket(sm);
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Instance && ((Instance) obj).getId() == this.getId();
	}

	@Override
	public String toString()
	{
		return this._template.getName() + "(" + this._id + ")";
	}
}
