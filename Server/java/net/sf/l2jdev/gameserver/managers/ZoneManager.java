package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.zone.AbstractZoneSettings;
import net.sf.l2jdev.gameserver.model.zone.ZoneForm;
import net.sf.l2jdev.gameserver.model.zone.ZoneRegion;
import net.sf.l2jdev.gameserver.model.zone.ZoneRespawn;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.model.zone.form.ZoneCuboid;
import net.sf.l2jdev.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2jdev.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2jdev.gameserver.model.zone.type.ArenaZone;
import net.sf.l2jdev.gameserver.model.zone.type.CastleZone;
import net.sf.l2jdev.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2jdev.gameserver.model.zone.type.ConditionZone;
import net.sf.l2jdev.gameserver.model.zone.type.DamageZone;
import net.sf.l2jdev.gameserver.model.zone.type.DerbyTrackZone;
import net.sf.l2jdev.gameserver.model.zone.type.EffectZone;
import net.sf.l2jdev.gameserver.model.zone.type.FishingZone;
import net.sf.l2jdev.gameserver.model.zone.type.FortZone;
import net.sf.l2jdev.gameserver.model.zone.type.HqZone;
import net.sf.l2jdev.gameserver.model.zone.type.JailZone;
import net.sf.l2jdev.gameserver.model.zone.type.LandingZone;
import net.sf.l2jdev.gameserver.model.zone.type.MotherTreeZone;
import net.sf.l2jdev.gameserver.model.zone.type.NoLandingZone;
import net.sf.l2jdev.gameserver.model.zone.type.NoRestartZone;
import net.sf.l2jdev.gameserver.model.zone.type.NoStoreZone;
import net.sf.l2jdev.gameserver.model.zone.type.NoSummonFriendZone;
import net.sf.l2jdev.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2jdev.gameserver.model.zone.type.PeaceZone;
import net.sf.l2jdev.gameserver.model.zone.type.PrisonZone;
import net.sf.l2jdev.gameserver.model.zone.type.ResidenceHallTeleportZone;
import net.sf.l2jdev.gameserver.model.zone.type.ResidenceTeleportZone;
import net.sf.l2jdev.gameserver.model.zone.type.ResidenceZone;
import net.sf.l2jdev.gameserver.model.zone.type.RespawnZone;
import net.sf.l2jdev.gameserver.model.zone.type.SayuneZone;
import net.sf.l2jdev.gameserver.model.zone.type.ScriptZone;
import net.sf.l2jdev.gameserver.model.zone.type.SiegableHallZone;
import net.sf.l2jdev.gameserver.model.zone.type.SiegeZone;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;
import net.sf.l2jdev.gameserver.model.zone.type.SwampZone;
import net.sf.l2jdev.gameserver.model.zone.type.TaxZone;
import net.sf.l2jdev.gameserver.model.zone.type.TeleportZone;
import net.sf.l2jdev.gameserver.model.zone.type.TimedHuntingZone;
import net.sf.l2jdev.gameserver.model.zone.type.UndyingZone;
import net.sf.l2jdev.gameserver.model.zone.type.WaterZone;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ZoneManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ZoneManager.class.getName());
	private static final Map<String, AbstractZoneSettings> SETTINGS = new HashMap<>();
	public static final int SHIFT_BY = 15;
	private static final int OFFSET_X = Math.abs(-9);
	private static final int OFFSET_Y = Math.abs(-8);
	private final Map<Class<? extends ZoneType>, ConcurrentHashMap<Integer, ? extends ZoneType>> _classZones = new ConcurrentHashMap<>();
	private final Map<String, SpawnTerritory> _spawnTerritories = new ConcurrentHashMap<>();
	private final AtomicInteger _lastDynamicId = new AtomicInteger(300000);
	private List<Item> _debugItems;
	private final ZoneRegion[][] _zoneRegions = new ZoneRegion[9 + OFFSET_X + 1][9 + OFFSET_Y + 1];

	protected ZoneManager()
	{
		for (int x = 0; x < this._zoneRegions.length; x++)
		{
			for (int y = 0; y < this._zoneRegions[x].length; y++)
			{
				this._zoneRegions[x][y] = new ZoneRegion(x, y);
			}
		}

		LOGGER.info(this.getClass().getSimpleName() + " " + this._zoneRegions.length + " by " + this._zoneRegions[0].length + " Zone Region Grid set up.");
		this.load();
	}

	@Override
	public void load()
	{
		this._classZones.clear();
		this._classZones.put(ArenaZone.class, new ConcurrentHashMap<>());
		this._classZones.put(CastleZone.class, new ConcurrentHashMap<>());
		this._classZones.put(ClanHallZone.class, new ConcurrentHashMap<>());
		this._classZones.put(ConditionZone.class, new ConcurrentHashMap<>());
		this._classZones.put(DamageZone.class, new ConcurrentHashMap<>());
		this._classZones.put(DerbyTrackZone.class, new ConcurrentHashMap<>());
		this._classZones.put(EffectZone.class, new ConcurrentHashMap<>());
		this._classZones.put(FishingZone.class, new ConcurrentHashMap<>());
		this._classZones.put(FortZone.class, new ConcurrentHashMap<>());
		this._classZones.put(HqZone.class, new ConcurrentHashMap<>());
		this._classZones.put(JailZone.class, new ConcurrentHashMap<>());
		this._classZones.put(LandingZone.class, new ConcurrentHashMap<>());
		this._classZones.put(MotherTreeZone.class, new ConcurrentHashMap<>());
		this._classZones.put(NoLandingZone.class, new ConcurrentHashMap<>());
		this._classZones.put(NoRestartZone.class, new ConcurrentHashMap<>());
		this._classZones.put(NoStoreZone.class, new ConcurrentHashMap<>());
		this._classZones.put(NoSummonFriendZone.class, new ConcurrentHashMap<>());
		this._classZones.put(OlympiadStadiumZone.class, new ConcurrentHashMap<>());
		this._classZones.put(PeaceZone.class, new ConcurrentHashMap<>());
		this._classZones.put(PrisonZone.class, new ConcurrentHashMap<>());
		this._classZones.put(ResidenceHallTeleportZone.class, new ConcurrentHashMap<>());
		this._classZones.put(ResidenceTeleportZone.class, new ConcurrentHashMap<>());
		this._classZones.put(ResidenceZone.class, new ConcurrentHashMap<>());
		this._classZones.put(RespawnZone.class, new ConcurrentHashMap<>());
		this._classZones.put(SayuneZone.class, new ConcurrentHashMap<>());
		this._classZones.put(ScriptZone.class, new ConcurrentHashMap<>());
		this._classZones.put(SiegableHallZone.class, new ConcurrentHashMap<>());
		this._classZones.put(SiegeZone.class, new ConcurrentHashMap<>());
		this._classZones.put(SwampZone.class, new ConcurrentHashMap<>());
		this._classZones.put(TaxZone.class, new ConcurrentHashMap<>());
		this._classZones.put(TeleportZone.class, new ConcurrentHashMap<>());
		this._classZones.put(TimedHuntingZone.class, new ConcurrentHashMap<>());
		this._classZones.put(UndyingZone.class, new ConcurrentHashMap<>());
		this._classZones.put(WaterZone.class, new ConcurrentHashMap<>());
		this._spawnTerritories.clear();
		this.parseDatapackDirectory("data/zones", false);
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._classZones.size() + " zone classes and " + this.getSize() + " zones.");
		OptionalInt maxId = this._classZones.values().stream().flatMap(map -> map.keySet().stream()).mapToInt(Integer.class::cast).filter(value -> value < 300000).max();
		LOGGER.info(this.getClass().getSimpleName() + ": Last static id " + maxId.getAsInt() + ".");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		List<int[]> rs = new ArrayList<>();

		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				NamedNodeMap attrs = n.getAttributes();
				Node attribute = attrs.getNamedItem("enabled");
				if (attribute == null || Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("zone".equalsIgnoreCase(d.getNodeName()))
						{
							attrs = d.getAttributes();
							attribute = attrs.getNamedItem("type");
							if (attribute != null)
							{
								String zoneType = attribute.getNodeValue();
								attribute = attrs.getNamedItem("id");
								int zoneId;
								if (attribute != null)
								{
									zoneId = Integer.parseInt(attribute.getNodeValue());
								}
								else
								{
									zoneId = zoneType.equalsIgnoreCase("NpcSpawnTerritory") ? 0 : this._lastDynamicId.incrementAndGet();
								}

								attribute = attrs.getNamedItem("name");
								String zoneName;
								if (attribute != null)
								{
									zoneName = attribute.getNodeValue();
								}
								else
								{
									zoneName = null;
								}

								if (zoneType.equalsIgnoreCase("NpcSpawnTerritory"))
								{
									if (zoneName == null)
									{
										LOGGER.warning("ZoneData: Missing name for NpcSpawnTerritory in file: " + file.getName() + ", skipping zone");
										continue;
									}

									if (this._spawnTerritories.containsKey(zoneName))
									{
										LOGGER.warning("ZoneData: Name " + zoneName + " already used for another zone, check file: " + file.getName() + ". Skipping zone");
										continue;
									}
								}

								int minZ = this.parseInteger(attrs, "minZ");
								int maxZ = this.parseInteger(attrs, "maxZ");
								zoneType = this.parseString(attrs, "type");
								String zoneShape = this.parseString(attrs, "shape");
								ZoneForm zoneForm = null;

								try
								{
									for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
									{
										if ("node".equalsIgnoreCase(cd.getNodeName()))
										{
											attrs = cd.getAttributes();
											int[] point = new int[]
											{
												this.parseInteger(attrs, "X"),
												this.parseInteger(attrs, "Y")
											};
											rs.add(point);
										}
									}

									int[][] coords = rs.toArray(new int[rs.size()][2]);
									rs.clear();
									if (coords == null || coords.length == 0)
									{
										LOGGER.warning(this.getClass().getSimpleName() + ": ZoneData: missing data for zone: " + zoneId + " XML file: " + file.getName());
										continue;
									}

									if (zoneShape.equalsIgnoreCase("Cuboid"))
									{
										if (coords.length != 2)
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": ZoneData: Missing cuboid vertex data for zone: " + zoneId + " in file: " + file.getName());
											continue;
										}

										zoneForm = new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ);
									}
									else if (zoneShape.equalsIgnoreCase("NPoly"))
									{
										if (coords.length <= 2)
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": ZoneData: Bad data for zone: " + zoneId + " in file: " + file.getName());
											continue;
										}

										int[] aX = new int[coords.length];
										int[] aY = new int[coords.length];

										for (int i = 0; i < coords.length; i++)
										{
											aX[i] = coords[i][0];
											aY[i] = coords[i][1];
										}

										zoneForm = new ZoneNPoly(aX, aY, minZ, maxZ);
									}
									else
									{
										if (!zoneShape.equalsIgnoreCase("Cylinder"))
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": ZoneData: Unknown shape: \"" + zoneShape + "\"  for zone: " + zoneId + " in file: " + file.getName());
											continue;
										}

										attrs = d.getAttributes();
										int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
										if (coords.length != 1 || zoneRad <= 0)
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": ZoneData: Bad data for zone: " + zoneId + " in file: " + file.getName());
											continue;
										}

										zoneForm = new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad);
									}
								}
								catch (Exception var26)
								{
									LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": ZoneData: Failed to load zone " + zoneId + " coordinates: " + var26.getMessage(), var26);
								}

								if (zoneType.equalsIgnoreCase("NpcSpawnTerritory"))
								{
									this._spawnTerritories.put(zoneName, new SpawnTerritory(zoneName, zoneForm));
								}
								else
								{
									Class<?> newZone = null;
									Constructor<?> zoneConstructor = null;

									ZoneType temp;
									try
									{
										newZone = Class.forName("net.sf.l2jdev.gameserver.model.zone.type." + zoneType);
										zoneConstructor = newZone.getConstructor(int.class);
										temp = (ZoneType) zoneConstructor.newInstance(zoneId);
										temp.setZone(zoneForm);
									}
									catch (Exception var25)
									{
										LOGGER.warning(this.getClass().getSimpleName() + ": ZoneData: No such zone type: " + zoneType + " in file: " + file.getName());
										continue;
									}

									for (Node cdx = d.getFirstChild(); cdx != null; cdx = cdx.getNextSibling())
									{
										if ("stat".equalsIgnoreCase(cdx.getNodeName()))
										{
											attrs = cdx.getAttributes();
											String name = attrs.getNamedItem("name").getNodeValue();
											String val = attrs.getNamedItem("val").getNodeValue();
											temp.setParameter(name, val);
										}
										else if ("spawn".equalsIgnoreCase(cdx.getNodeName()) && temp instanceof ZoneRespawn)
										{
											attrs = cdx.getAttributes();
											int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
											int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
											int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
											Node val = attrs.getNamedItem("type");
											((ZoneRespawn) temp).parseLoc(spawnX, spawnY, spawnZ, val == null ? null : val.getNodeValue());
										}
										else if ("race".equalsIgnoreCase(cdx.getNodeName()) && temp instanceof RespawnZone)
										{
											attrs = cdx.getAttributes();
											String race = attrs.getNamedItem("name").getNodeValue();
											String point = attrs.getNamedItem("point").getNodeValue();
											((RespawnZone) temp).addRaceRespawnPoint(race, point);
										}
									}

									if (this.checkId(zoneId))
									{
										LOGGER.config(this.getClass().getSimpleName() + ": Caution: Zone (" + zoneId + ") from file: " + file.getName() + " overrides previous definition.");
									}

									if (zoneName != null && !zoneName.isEmpty())
									{
										temp.setName(zoneName);
									}

									this.addZone(zoneId, temp);

									for (int x = 0; x < this._zoneRegions.length; x++)
									{
										for (int y = 0; y < this._zoneRegions[x].length; y++)
										{
											int ax = x - OFFSET_X << 15;
											int bx = x + 1 - OFFSET_X << 15;
											int ay = y - OFFSET_Y << 15;
											int by = y + 1 - OFFSET_Y << 15;
											if (temp.getZone().intersectsRectangle(ax, bx, ay, by))
											{
												this._zoneRegions[x][y].getZones().put(temp.getId(), temp);
											}
										}
									}
								}
							}
							else
							{
								LOGGER.warning("ZoneData: Missing type for zone in file: " + file.getName());
							}
						}
					}
				}
			}
		}
	}

	public void reload()
	{
		this.unload();
		this.load();

		for (WorldObject obj : World.getInstance().getVisibleObjects())
		{
			if (obj.isCreature())
			{
				obj.asCreature().revalidateZone(true);
			}
		}

		SETTINGS.clear();
	}

	public void unload()
	{
		int count = 0;

		for (Map<Integer, ? extends ZoneType> map : this._classZones.values())
		{
			for (ZoneType zone : map.values())
			{
				if (zone.getSettings() != null)
				{
					SETTINGS.put(zone.getName(), zone.getSettings());
				}
			}
		}

		for (ZoneRegion[] zoneRegions : this._zoneRegions)
		{
			for (ZoneRegion zoneRegion : zoneRegions)
			{
				zoneRegion.getZones().clear();
				count++;
			}
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Removed zones in " + count + " regions.");
	}

	public int getSize()
	{
		int i = 0;

		for (Map<Integer, ? extends ZoneType> map : this._classZones.values())
		{
			i += map.size();
		}

		return i;
	}

	private boolean checkId(int id)
	{
		for (Map<Integer, ? extends ZoneType> map : this._classZones.values())
		{
			if (map.containsKey(id))
			{
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private <T extends ZoneType> void addZone(Integer id, T zone)
	{
		ConcurrentHashMap<Integer, T> map = (ConcurrentHashMap<Integer, T>) this._classZones.get(zone.getClass());
		if (map == null)
		{
			this._classZones.put(zone.getClass(), new ConcurrentHashMap<>());
			map = (ConcurrentHashMap<Integer, T>) this._classZones.get(zone.getClass());
		}

		map.put(id, zone);
	}

	@SuppressWarnings("unchecked")
	public <T extends ZoneType> Collection<T> getAllZones(Class<T> zoneType)
	{
		return (Collection<T>) this._classZones.get(zoneType).values();
	}

	public ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends ZoneType> map : this._classZones.values())
		{
			if (map.containsKey(id))
			{
				return map.get(id);
			}
		}

		return null;
	}

	public ZoneType getZoneByName(String name)
	{
		for (Map<Integer, ? extends ZoneType> map : this._classZones.values())
		{
			for (ZoneType zone : map.values())
			{
				if (zone.getName() != null && zone.getName().equals(name))
				{
					return zone;
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneById(int id, Class<T> zoneType)
	{
		return (T) this._classZones.get(zoneType).get(id);
	}

	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneByName(String name, Class<T> zoneType)
	{
		if (this._classZones.containsKey(zoneType))
		{
			for (ZoneType zone : this._classZones.get(zoneType).values())
			{
				if (zone.getName() != null && zone.getName().equals(name))
				{
					return (T) zone;
				}
			}
		}

		return null;
	}

	public List<ZoneType> getZones(ILocational locational)
	{
		return this.getZones(locational.getX(), locational.getY(), locational.getZ());
	}

	public <T extends ZoneType> T getZone(ILocational locational, Class<T> type)
	{
		return locational == null ? null : this.getZone(locational.getX(), locational.getY(), locational.getZ(), type);
	}

	public List<ZoneType> getZones(int x, int y)
	{
		ZoneRegion region = this.getRegion(x, y);
		if (region == null)
		{
			return Collections.emptyList();
		}
		List<ZoneType> result = new ArrayList<>();

		for (ZoneType zone : region.getZones().values())
		{
			if (zone.isInsideZone(x, y))
			{
				result.add(zone);
			}
		}

		return result;
	}

	public List<ZoneType> getZones(int x, int y, int z)
	{
		ZoneRegion region = this.getRegion(x, y);
		if (region == null)
		{
			return Collections.emptyList();
		}
		List<ZoneType> result = new ArrayList<>();

		for (ZoneType zone : region.getZones().values())
		{
			if (zone.isInsideZone(x, y, z))
			{
				result.add(zone);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		ZoneRegion region = this.getRegion(x, y);
		if (region == null)
		{
			return null;
		}
		for (ZoneType zone : region.getZones().values())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
			{
				return (T) zone;
			}
		}

		return null;
	}

	public SpawnTerritory getSpawnTerritory(String name)
	{
		return this._spawnTerritories.get(name);
	}

	public List<SpawnTerritory> getSpawnTerritories(WorldObject object)
	{
		List<SpawnTerritory> result = new ArrayList<>();

		for (SpawnTerritory territory : this._spawnTerritories.values())
		{
			if (territory.isInsideZone(object.getX(), object.getY(), object.getZ()))
			{
				result.add(territory);
			}
		}

		return result;
	}

	public OlympiadStadiumZone getOlympiadStadium(Creature creature)
	{
		if (creature == null)
		{
			return null;
		}
		for (ZoneType zone : getInstance().getZones(creature.getX(), creature.getY(), creature.getZ()))
		{
			if (zone instanceof OlympiadStadiumZone && zone.isCharacterInZone(creature))
			{
				return (OlympiadStadiumZone) zone;
			}
		}

		return null;
	}

	public List<Item> getDebugItems()
	{
		if (this._debugItems == null)
		{
			this._debugItems = new ArrayList<>();
		}

		return this._debugItems;
	}

	public synchronized void clearDebugItems()
	{
		if (this._debugItems != null)
		{
			for (Item item : this._debugItems)
			{
				if (item != null)
				{
					item.decayMe();
				}
			}

			this._debugItems.clear();
		}
	}

	public ZoneRegion getRegion(int x, int y)
	{
		try
		{
			return this._zoneRegions[(x >> 15) + OFFSET_X][(y >> 15) + OFFSET_Y];
		}
		catch (ArrayIndexOutOfBoundsException var4)
		{
			return null;
		}
	}

	public ZoneRegion getRegion(ILocational point)
	{
		return this.getRegion(point.getX(), point.getY());
	}

	public static AbstractZoneSettings getSettings(String name)
	{
		return SETTINGS.get(name);
	}

	public static ZoneManager getInstance()
	{
		return ZoneManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ZoneManager INSTANCE = new ZoneManager();
	}
}
