package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.MapRegion;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.zone.type.RespawnZone;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MapRegionManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MapRegionManager.class.getName());
	private static final Map<String, MapRegion> REGIONS = new HashMap<>();
	public static final String DEFAULT_RESPAWN = "talking_island_town";

	protected MapRegionManager()
	{
		this.load();
	}

	@Override
	public void load()
	{
		REGIONS.clear();
		this.parseDatapackDirectory("data/mapregion", false);
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + REGIONS.size() + " map regions.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("region".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						String name = attrs.getNamedItem("name").getNodeValue();
						String town = attrs.getNamedItem("town").getNodeValue();
						int locId = this.parseInteger(attrs, "locId");
						int bbs = this.parseInteger(attrs, "bbs");
						MapRegion region = new MapRegion(name, town, locId, bbs);

						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							attrs = c.getAttributes();
							if ("respawnPoint".equalsIgnoreCase(c.getNodeName()))
							{
								int spawnX = this.parseInteger(attrs, "X");
								int spawnY = this.parseInteger(attrs, "Y");
								int spawnZ = this.parseInteger(attrs, "Z");
								boolean other = this.parseBoolean(attrs, "isOther", false);
								boolean chaotic = this.parseBoolean(attrs, "isChaotic", false);
								boolean banish = this.parseBoolean(attrs, "isBanish", false);
								if (other)
								{
									region.addOtherSpawn(spawnX, spawnY, spawnZ);
								}
								else if (chaotic)
								{
									region.addChaoticSpawn(spawnX, spawnY, spawnZ);
								}
								else if (banish)
								{
									region.addBanishSpawn(spawnX, spawnY, spawnZ);
								}
								else
								{
									region.addSpawn(spawnX, spawnY, spawnZ);
								}
							}
							else if ("map".equalsIgnoreCase(c.getNodeName()))
							{
								region.addMap(this.parseInteger(attrs, "X"), this.parseInteger(attrs, "Y"));
							}
							else if ("banned".equalsIgnoreCase(c.getNodeName()))
							{
								region.addBannedRace(attrs.getNamedItem("race").getNodeValue(), attrs.getNamedItem("point").getNodeValue());
							}
						}

						REGIONS.put(name, region);
					}
				}
			}
		}
	}

	public MapRegion getMapRegion(int locX, int locY)
	{
		for (MapRegion region : REGIONS.values())
		{
			if (region.isZoneInRegion(this.getMapRegionX(locX), this.getMapRegionY(locY)))
			{
				return region;
			}
		}

		return null;
	}

	public int getMapRegionLocId(int locX, int locY)
	{
		MapRegion region = this.getMapRegion(locX, locY);
		return region != null ? region.getLocId() : 0;
	}

	public MapRegion getMapRegion(WorldObject obj)
	{
		return this.getMapRegion(obj.getX(), obj.getY());
	}

	public int getMapRegionLocId(WorldObject obj)
	{
		return this.getMapRegionLocId(obj.getX(), obj.getY());
	}

	public int getMapRegionX(int posX)
	{
		return (posX >> 15) + 9 + 11;
	}

	public int getMapRegionY(int posY)
	{
		return (posY >> 15) + 10 + 8;
	}

	public String getClosestTownName(Creature creature)
	{
		MapRegion region = this.getMapRegion(creature);
		return region == null ? "Aden Castle Town" : region.getTown();
	}

	public Location getTeleToLocation(Creature creature, TeleportWhereType teleportWhere)
	{
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;
			Clan clan = player.getClan();
			if (clan != null && !player.isFlyingMounted() && !player.isFlying())
			{
				if (teleportWhere == TeleportWhereType.CLANHALL)
				{
					clanhall = ClanHallData.getInstance().getClanHallByClan(clan);
					if (clanhall != null && !player.isFlyingMounted())
					{
						return clanhall.getOwnerLocation();
					}
				}

				if (teleportWhere == TeleportWhereType.CASTLE)
				{
					castle = CastleManager.getInstance().getCastleByOwner(clan);
					if (castle == null)
					{
						castle = CastleManager.getInstance().getCastle(player);
						if (castle == null || !castle.getSiege().isInProgress() || castle.getSiege().getDefenderClan(clan) == null)
						{
							castle = null;
						}
					}

					if (castle != null && castle.getResidenceId() > 0)
					{
						if (player.getReputation() < 0)
						{
							return castle.getResidenceZone().getChaoticSpawnLoc();
						}

						return castle.getResidenceZone().getSpawnLoc();
					}
				}

				if (teleportWhere == TeleportWhereType.FORTRESS)
				{
					fort = FortManager.getInstance().getFortByOwner(clan);
					if (fort == null)
					{
						fort = FortManager.getInstance().getFort(player);
						if (fort == null || !fort.getSiege().isInProgress() || fort.getOwnerClan() != clan)
						{
							fort = null;
						}
					}

					if (fort != null && fort.getResidenceId() > 0)
					{
						if (player.getReputation() < 0)
						{
							return fort.getResidenceZone().getChaoticSpawnLoc();
						}

						return fort.getResidenceZone().getSpawnLoc();
					}
				}

				if (teleportWhere == TeleportWhereType.SIEGEFLAG)
				{
					castle = CastleManager.getInstance().getCastle(player);
					fort = FortManager.getInstance().getFort(player);
					if (castle != null)
					{
						if (castle.getSiege().isInProgress())
						{
							Set<Npc> flags = castle.getSiege().getFlag(clan);
							if (flags != null && !flags.isEmpty())
							{
								return flags.stream().findAny().get().getLocation();
							}
						}
					}
					else if (fort != null && fort.getSiege().isInProgress())
					{
						Set<Npc> flags = fort.getSiege().getFlag(clan);
						if (flags != null && !flags.isEmpty())
						{
							return flags.stream().findAny().get().getLocation();
						}
					}
				}
			}

			TimedHuntingZoneHolder timedHuntingZone = player.getTimedHuntingZone();
			if (timedHuntingZone != null)
			{
				Location exitLocation = timedHuntingZone.getExitLocation();
				if (exitLocation != null)
				{
					return exitLocation;
				}
			}

			if (player.getReputation() < 0)
			{
				return this.getNearestKarmaRespawn(player);
			}

			castle = CastleManager.getInstance().getCastle(player);
			if (castle != null && castle.getSiege().isInProgress() && (castle.getSiege().checkIsDefender(clan) || castle.getSiege().checkIsAttacker(clan)))
			{
				return castle.getResidenceZone().getOtherSpawnLoc();
			}

			Instance inst = player.getInstanceWorld();
			if (inst != null)
			{
				Location loc = inst.getExitLocation(player);
				if (loc != null)
				{
					return loc;
				}
			}

			if (FactionSystemConfig.FACTION_SYSTEM_ENABLED && FactionSystemConfig.FACTION_RESPAWN_AT_BASE)
			{
				if (player.isGood())
				{
					return FactionSystemConfig.FACTION_GOOD_BASE_LOCATION;
				}

				if (player.isEvil())
				{
					return FactionSystemConfig.FACTION_EVIL_BASE_LOCATION;
				}
			}
		}

		return this.getNearestTownRespawn(creature);
	}

	public Location getNearestKarmaRespawn(Player player)
	{
		try
		{
			RespawnZone zone = ZoneManager.getInstance().getZone(player, RespawnZone.class);
			if (zone != null)
			{
				return this.getRestartRegion(player, zone.getRespawnPoint(player)).getChaoticSpawnLoc();
			}
			return this.getMapRegion(player).getBannedRace().containsKey(player.getRace()) ? REGIONS.get(this.getMapRegion(player).getBannedRace().get(player.getRace())).getChaoticSpawnLoc() : this.getMapRegion(player).getChaoticSpawnLoc();
		}
		catch (Exception var3)
		{
			return player.isFlyingMounted() ? REGIONS.get("union_base_of_kserth").getChaoticSpawnLoc() : REGIONS.get("talking_island_town").getChaoticSpawnLoc();
		}
	}

	public Location getNearestTownRespawn(Creature creature)
	{
		try
		{
			RespawnZone zone = ZoneManager.getInstance().getZone(creature, RespawnZone.class);
			if (zone != null)
			{
				return this.getRestartRegion(creature, zone.getRespawnPoint(creature.asPlayer())).getSpawnLoc();
			}
			return this.getMapRegion(creature).getBannedRace().containsKey(creature.getRace()) ? REGIONS.get(this.getMapRegion(creature).getBannedRace().get(creature.getRace())).getSpawnLoc() : this.getMapRegion(creature).getSpawnLoc();
		}
		catch (Exception var3)
		{
			return REGIONS.get("talking_island_town").getSpawnLoc();
		}
	}

	public MapRegion getRestartRegion(Creature creature, String point)
	{
		try
		{
			Player player = creature.asPlayer();
			MapRegion region = REGIONS.get(point);
			if (region.getBannedRace().containsKey(player.getRace()))
			{
				this.getRestartRegion(player, region.getBannedRace().get(player.getRace()));
			}

			return region;
		}
		catch (Exception var5)
		{
			return REGIONS.get("talking_island_town");
		}
	}

	public MapRegion getMapRegionByName(String regionName)
	{
		return REGIONS.get(regionName);
	}

	public int getBBs(ILocational loc)
	{
		MapRegion region = this.getMapRegion(loc.getX(), loc.getY());
		return region != null ? region.getBbs() : REGIONS.get("talking_island_town").getBbs();
	}

	public static MapRegionManager getInstance()
	{
		return MapRegionManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final MapRegionManager INSTANCE = new MapRegionManager();
	}
}
