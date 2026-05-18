package net.sf.l2jdev.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.config.InterfaceConfig;
import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;

public class World
{
	private static final Logger LOGGER = Logger.getLogger(World.class.getName());
	public static volatile int MAX_CONNECTED_COUNT = 0;
	public static volatile int OFFLINE_TRADE_COUNT = 0;
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;
	public static final int SHIFT_BY = 11;
	public static final int TILE_SIZE = 32768;
	public static final int TILE_X_MIN = 11;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_X_MAX = 28;
	public static final int TILE_Y_MAX = 26;
	public static final int TILE_ZERO_COORD_X = 20;
	public static final int TILE_ZERO_COORD_Y = 18;
	public static final int WORLD_X_MIN = -294912;
	public static final int WORLD_Y_MIN = -262144;
	public static final int WORLD_X_MAX = 294912;
	public static final int WORLD_Y_MAX = 294912;
	public static final int WORLD_Z_MIN = -16000;
	public static final int WORLD_Z_MAX = 16000;
	public static final int Z_REGION_SIZE = 2000;
	public static final int OFFSET_X = Math.abs(-144);
	public static final int OFFSET_Y = Math.abs(-128);
	private static final int REGIONS_X = 144 + OFFSET_X;
	private static final int REGIONS_Y = 144 + OFFSET_Y;
	private static final int REGIONS_Z = (Math.abs(-16000) + Math.abs(16000)) / 2000;
	private static final Map<Integer, Player> _allPlayers = new ConcurrentHashMap<>();
	private static final Map<Integer, Player> _allGoodPlayers = new ConcurrentHashMap<>();
	private static final Map<Integer, Player> _allEvilPlayers = new ConcurrentHashMap<>();
	private static final Map<Integer, Player> _allStoreModeBuySellPlayers = new ConcurrentHashMap<>();
	private static final Map<Integer, WorldObject> _allObjects = new ConcurrentHashMap<>();
	private static final Map<Integer, Pet> _petsInstance = new ConcurrentHashMap<>();
	private static final AtomicInteger _partyNumber = new AtomicInteger();
	private static final AtomicInteger _memberInPartyNumber = new AtomicInteger();
	private static final Set<Player> _pkPlayers = ConcurrentHashMap.newKeySet(30);
	private static final AtomicInteger _lastPkTime = new AtomicInteger((int) System.currentTimeMillis() / 1000);
	private static final WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z];
	private static long _nextPrivateStoreUpdate = 0L;

	protected World()
	{
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int z = 0; z < REGIONS_Z; z++)
				{
					_worldRegions[x][y][z] = new WorldRegion(x, y, z);
				}
			}
		}

		for (int rx = 0; rx <= REGIONS_X; rx++)
		{
			for (int ry = 0; ry <= REGIONS_Y; ry++)
			{
				for (int rz = 0; rz < REGIONS_Z; rz++)
				{
					List<WorldRegion> surroundingRegions = new ArrayList<>();

					for (int sx = rx - 1; sx <= rx + 1; sx++)
					{
						for (int sy = ry - 1; sy <= ry + 1; sy++)
						{
							for (int sz = rz - 1; sz <= rz + 1; sz++)
							{
								if (sx >= 0 && sx < REGIONS_X && sy >= 0 && sy < REGIONS_Y && sz >= 0 && sz < REGIONS_Z)
								{
									surroundingRegions.add(_worldRegions[sx][sy][sz]);
								}
							}
						}
					}

					WorldRegion[] regionArray = new WorldRegion[surroundingRegions.size()];
					regionArray = surroundingRegions.toArray(regionArray);
					_worldRegions[rx][ry][rz].setSurroundingRegions(regionArray);
				}
			}
		}

		if (!InterfaceConfig.ENABLE_GUI)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
		}
	}

	public void addObject(WorldObject object)
	{
		_allObjects.putIfAbsent(object.getObjectId(), object);
		if (object.isPlayer())
		{
			Player newPlayer = object.asPlayer();
			if (newPlayer.isTeleporting())
			{
				return;
			}

			Player existingPlayer = _allPlayers.putIfAbsent(object.getObjectId(), newPlayer);
			if (existingPlayer != null)
			{
				Disconnection.of(existingPlayer).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
				Disconnection.of(newPlayer).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
				LOGGER.warning(this.getClass().getSimpleName() + ": Duplicate character!? Disconnected both characters (" + newPlayer.getName() + ")");
			}
			else if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
			{
				addFactionPlayerToWorld(newPlayer);
			}
		}
	}

	public void removeObject(WorldObject object)
	{
		_allObjects.remove(object.getObjectId());
		if (object.isPlayer())
		{
			Player player = object.asPlayer();
			if (player.isTeleporting())
			{
				return;
			}

			_allPlayers.remove(object.getObjectId());
			if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
			{
				if (player.isGood())
				{
					_allGoodPlayers.remove(player.getObjectId());
				}
				else if (player.isEvil())
				{
					_allEvilPlayers.remove(player.getObjectId());
				}
			}
		}
	}

	public WorldObject findObject(int objectId)
	{
		return _allObjects.get(objectId);
	}

	public Collection<WorldObject> getVisibleObjects()
	{
		return _allObjects.values();
	}

	public int getVisibleObjectsCount()
	{
		return _allObjects.size();
	}

	public Collection<Player> getPlayers()
	{
		return _allPlayers.values();
	}

	public Collection<Player> getAllGoodPlayers()
	{
		return _allGoodPlayers.values();
	}

	public Collection<Player> getAllEvilPlayers()
	{
		return _allEvilPlayers.values();
	}

	public Player getPlayer(String name)
	{
		return this.getPlayer(CharInfoTable.getInstance().getIdByName(name));
	}

	public Player getPlayer(int objectId)
	{
		return _allPlayers.get(objectId);
	}

	public Pet getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}

	public synchronized Collection<Player> getSellingOrBuyingPlayers()
	{
		long currentTime = System.currentTimeMillis();
		if (currentTime > _nextPrivateStoreUpdate)
		{
			_nextPrivateStoreUpdate = currentTime + GeneralConfig.STORE_REVIEW_CACHE_TIME;
			_allStoreModeBuySellPlayers.clear();

			for (Player player : _allPlayers.values())
			{
				if (player.isInStoreSellOrBuyMode())
				{
					_allStoreModeBuySellPlayers.put(player.getObjectId(), player);
				}
			}
		}

		return _allStoreModeBuySellPlayers.values();
	}

	public Pet addPet(int ownerId, Pet pet)
	{
		return _petsInstance.put(ownerId, pet);
	}

	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}

	public Npc getNpc(int npcId)
	{
		for (WorldObject wo : this.getVisibleObjects())
		{
			if (wo.isNpc() && wo.getId() == npcId)
			{
				return wo.asNpc();
			}
		}

		return null;
	}

	public void addVisibleObject(WorldObject object, WorldRegion newRegion)
	{
		if (newRegion.isActive())
		{
			this.forEachVisibleObject(object, WorldObject.class, wo -> {
				if (object.isPlayer() && wo.isVisibleFor(object.asPlayer()))
				{
					wo.sendInfo(object.asPlayer());
					if (wo.isCreature())
					{
						CreatureAI ai = wo.asCreature().getAI();
						if (ai != null)
						{
							ai.describeStateToPlayer(object.asPlayer());
							if (wo.isMonster() && ai.getIntention() == Intention.IDLE)
							{
								ai.setIntention(Intention.ACTIVE);
							}
						}
					}
				}

				if (wo.isPlayer() && object.isVisibleFor(wo.asPlayer()))
				{
					object.sendInfo(wo.asPlayer());
					if (object.isCreature())
					{
						CreatureAI ai = object.asCreature().getAI();
						if (ai != null)
						{
							ai.describeStateToPlayer(wo.asPlayer());
							if (object.isMonster() && ai.getIntention() == Intention.IDLE)
							{
								ai.setIntention(Intention.ACTIVE);
							}
						}
					}
				}
			});
		}
	}

	public static void addFactionPlayerToWorld(Player player)
	{
		if (player.isGood())
		{
			_allGoodPlayers.put(player.getObjectId(), player);
		}
		else if (player.isEvil())
		{
			_allEvilPlayers.put(player.getObjectId(), player);
		}
	}

	public void removeVisibleObject(WorldObject object, WorldRegion oldRegion)
	{
		if (object != null && oldRegion != null)
		{
			oldRegion.removeVisibleObject(object);
			WorldRegion[] surroundingRegions = oldRegion.getSurroundingRegions();

			for (WorldRegion surroundingRegion : surroundingRegions)
			{
				Collection<WorldObject> visibleObjects = surroundingRegion.getVisibleObjects();
				if (!visibleObjects.isEmpty())
				{
					for (WorldObject wo : visibleObjects)
					{
						if (wo != object)
						{
							if (object.isCreature())
							{
								Creature objectCreature = object.asCreature();
								CreatureAI ai = objectCreature.getAI();
								if (ai != null)
								{
									ai.notifyAction(Action.FORGET_OBJECT, wo);
								}

								if (objectCreature.getTarget() == wo)
								{
									objectCreature.setTarget(null);
								}

								if (object.isPlayer())
								{
									object.sendPacket(new DeleteObject(wo));
								}
							}

							if (wo.isCreature())
							{
								Creature woCreature = wo.asCreature();
								CreatureAI aix = woCreature.getAI();
								if (aix != null)
								{
									aix.notifyAction(Action.FORGET_OBJECT, object);
								}

								if (woCreature.getTarget() == object)
								{
									woCreature.setTarget(null);
								}

								if (wo.isPlayer())
								{
									wo.sendPacket(new DeleteObject(object));
								}
							}
						}
					}
				}
			}
		}
	}

	public void switchRegion(WorldObject object, WorldRegion newRegion)
	{
		WorldRegion oldRegion = object.getWorldRegion();
		if (oldRegion != null && oldRegion != newRegion)
		{
			WorldRegion[] oldSurroundingRegions = oldRegion.getSurroundingRegions();

			for (WorldRegion worldRegion : oldSurroundingRegions)
			{
				if (!newRegion.isSurroundingRegion(worldRegion))
				{
					Collection<WorldObject> visibleObjects = worldRegion.getVisibleObjects();
					if (!visibleObjects.isEmpty())
					{
						for (WorldObject wo : visibleObjects)
						{
							if (wo != object)
							{
								if (object.isCreature())
								{
									Creature objectCreature = object.asCreature();
									CreatureAI ai = objectCreature.getAI();
									if (ai != null)
									{
										ai.notifyAction(Action.FORGET_OBJECT, wo);
									}

									if (objectCreature.getTarget() == wo)
									{
										objectCreature.setTarget(null);
									}

									if (object.isPlayer())
									{
										object.sendPacket(new DeleteObject(wo));
									}
								}

								if (wo.isCreature())
								{
									Creature woCreature = wo.asCreature();
									CreatureAI aix = woCreature.getAI();
									if (aix != null)
									{
										aix.notifyAction(Action.FORGET_OBJECT, object);
									}

									if (woCreature.getTarget() == object)
									{
										woCreature.setTarget(null);
									}

									if (wo.isPlayer())
									{
										wo.sendPacket(new DeleteObject(object));
									}
								}
							}
						}
					}
				}
			}

			WorldRegion[] newSurroundingRegions = newRegion.getSurroundingRegions();

			for (WorldRegion worldRegion : newSurroundingRegions)
			{
				if (!oldRegion.isSurroundingRegion(worldRegion))
				{
					Collection<WorldObject> visibleObjects = worldRegion.getVisibleObjects();
					if (!visibleObjects.isEmpty())
					{
						for (WorldObject wox : visibleObjects)
						{
							if (wox != object && wox.getInstanceWorld() == object.getInstanceWorld())
							{
								if (object.isPlayer() && wox.isVisibleFor(object.asPlayer()))
								{
									wox.sendInfo(object.asPlayer());
									if (wox.isCreature())
									{
										CreatureAI aixx = wox.asCreature().getAI();
										if (aixx != null)
										{
											aixx.describeStateToPlayer(object.asPlayer());
											if (wox.isMonster() && aixx.getIntention() == Intention.IDLE)
											{
												aixx.setIntention(Intention.ACTIVE);
											}
										}
									}
								}

								if (wox.isPlayer() && object.isVisibleFor(wox.asPlayer()))
								{
									object.sendInfo(wox.asPlayer());
									if (object.isCreature())
									{
										CreatureAI aixx = object.asCreature().getAI();
										if (aixx != null)
										{
											aixx.describeStateToPlayer(wox.asPlayer());
											if (object.isMonster() && aixx.getIntention() == Intention.IDLE)
											{
												aixx.setIntention(Intention.ACTIVE);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public <T extends WorldObject> List<T> getVisibleObjects(WorldObject object, Class<T> clazz)
	{
		List<T> result = new LinkedList<>();
		this.forEachVisibleObject(object, clazz, result::add);
		return result;
	}

	public <T extends WorldObject> List<T> getVisibleObjects(WorldObject object, Class<T> clazz, Predicate<T> predicate)
	{
		List<T> result = new LinkedList<>();
		this.forEachVisibleObject(object, clazz, o -> {
			if (predicate.test(o))
			{
				result.add(o);
			}
		});
		return result;
	}

	public <T extends WorldObject> void forEachVisibleObject(WorldObject object, Class<T> clazz, Consumer<T> c)
	{
		if (object != null)
		{
			WorldRegion worldRegion = this.getRegion(object);
			if (worldRegion != null)
			{
				WorldRegion[] surroundingRegions = worldRegion.getSurroundingRegions();

				for (WorldRegion surroundingRegion : surroundingRegions)
				{
					Collection<WorldObject> visibleObjects = surroundingRegion.getVisibleObjects();
					if (!visibleObjects.isEmpty())
					{
						for (WorldObject wo : visibleObjects)
						{
							if (wo != object && clazz.isInstance(wo) && wo.getInstanceWorld() == object.getInstanceWorld())
							{
								c.accept(clazz.cast(wo));
							}
						}
					}
				}
			}
		}
	}

	public <T extends WorldObject> List<T> getVisibleObjectsInRange(WorldObject object, Class<T> clazz, int range)
	{
		List<T> result = new LinkedList<>();
		this.forEachVisibleObjectInRange(object, clazz, range, result::add);
		return result;
	}

	public <T extends WorldObject> List<T> getVisibleObjectsInRange(WorldObject object, Class<T> clazz, int range, Predicate<T> predicate)
	{
		List<T> result = new LinkedList<>();
		this.forEachVisibleObjectInRange(object, clazz, range, o -> {
			if (predicate.test(o))
			{
				result.add(o);
			}
		});
		return result;
	}

	public <T extends WorldObject> void forEachVisibleObjectInRange(WorldObject object, Class<T> clazz, int range, Consumer<T> c)
	{
		if (object != null)
		{
			WorldRegion worldRegion = this.getRegion(object);
			if (worldRegion != null)
			{
				WorldRegion[] surroundingRegions = worldRegion.getSurroundingRegions();

				for (WorldRegion surroundingRegion : surroundingRegions)
				{
					Collection<WorldObject> visibleObjects = surroundingRegion.getVisibleObjects();
					if (!visibleObjects.isEmpty())
					{
						for (WorldObject wo : visibleObjects)
						{
							if (wo != object && clazz.isInstance(wo) && wo.getInstanceWorld() == object.getInstanceWorld() && wo.calculateDistance3D(object) <= range)
							{
								c.accept(clazz.cast(wo));
							}
						}
					}
				}
			}
		}
	}

	public WorldRegion getRegion(WorldObject object)
	{
		try
		{
			int z = object.getZ();
			int regionZ = this.calculateRegionZ(z);
			return _worldRegions[(object.getX() >> 11) + OFFSET_X][(object.getY() >> 11) + OFFSET_Y][regionZ];
		}
		catch (ArrayIndexOutOfBoundsException var4)
		{
			this.disposeOutOfBoundsObject(object);
			return null;
		}
	}

	public WorldRegion getRegion(int x, int y, int z)
	{
		try
		{
			int regionZ = this.calculateRegionZ(z);
			return _worldRegions[(x >> 11) + OFFSET_X][(y >> 11) + OFFSET_Y][regionZ];
		}
		catch (ArrayIndexOutOfBoundsException var5)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Incorrect world region X: " + ((x >> 11) + OFFSET_X) + " Y: " + ((y >> 11) + OFFSET_Y) + " Z: " + this.calculateRegionZ(z));
			return null;
		}
	}

	public int calculateRegionZ(int z)
	{
		if (z < -16000)
		{
			return 0;
		}
		return z > 16000 ? REGIONS_Z - 1 : (z - -16000) / 2000;
	}

	public WorldRegion[][][] getWorldRegions()
	{
		return _worldRegions;
	}

	public synchronized void disposeOutOfBoundsObject(WorldObject object)
	{
		if (object.isPlayer())
		{
			object.asCreature().stopMove(object.asPlayer().getLastServerPosition());
		}
		else if (object.isSummon())
		{
			Summon summon = object.asSummon();
			summon.unSummon(summon.getOwner());
		}
		else if (_allObjects.remove(object.getObjectId()) != null)
		{
			if (object.isNpc())
			{
				Npc npc = object.asNpc();
				LOGGER.warning("Deleting npc " + object.getName() + " NPCID[" + npc.getId() + "] from invalid location X:" + object.getX() + " Y:" + object.getY() + " Z:" + object.getZ());
				npc.deleteMe();
				Spawn spawn = npc.getSpawn();
				if (spawn != null)
				{
					LOGGER.warning("Spawn location X:" + spawn.getX() + " Y:" + spawn.getY() + " Z:" + spawn.getZ() + " Heading:" + spawn.getHeading());
				}
			}
			else if (object.isCreature())
			{
				LOGGER.warning("Deleting object " + object.getName() + " OID[" + object.getObjectId() + "] from invalid location X:" + object.getX() + " Y:" + object.getY() + " Z:" + object.getZ());
				object.asCreature().deleteMe();
			}

			if (object.getWorldRegion() != null)
			{
				object.getWorldRegion().removeVisibleObject(object);
			}
		}
	}

	public void incrementParty()
	{
		_partyNumber.incrementAndGet();
	}

	public void decrementParty()
	{
		_partyNumber.decrementAndGet();
	}

	public void incrementPartyMember()
	{
		_memberInPartyNumber.incrementAndGet();
	}

	public void decrementPartyMember()
	{
		_memberInPartyNumber.decrementAndGet();
	}

	public int getPartyCount()
	{
		return _partyNumber.get();
	}

	public int getPartyMemberCount()
	{
		return _memberInPartyNumber.get();
	}

	public synchronized void addPkPlayer(Player player)
	{
		if (_pkPlayers.size() > 29)
		{
			Player lowestPk = null;
			int lowestPkCount = Integer.MAX_VALUE;

			for (Player pk : _pkPlayers)
			{
				if (pk.getPkKills() < lowestPkCount)
				{
					lowestPk = pk;
					lowestPkCount = pk.getPkKills();
				}
			}

			if (lowestPk == null || lowestPkCount >= player.getPkKills())
			{
				return;
			}

			_pkPlayers.remove(lowestPk);
		}

		_pkPlayers.add(player);
		_lastPkTime.set((int) System.currentTimeMillis() / 1000);
	}

	public void removePkPlayer(Player player)
	{
		_pkPlayers.remove(player);
		_lastPkTime.set((int) System.currentTimeMillis() / 1000);
	}

	public Set<Player> getPkPlayers()
	{
		return !FeatureConfig.PK_PENALTY_LIST ? Collections.emptySet() : _pkPlayers;
	}

	public int getLastPkTime()
	{
		return _lastPkTime.get();
	}

	public static World getInstance()
	{
		return World.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final World INSTANCE = new World();
	}
}
