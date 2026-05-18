package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanHallGrade;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanHallType;
import net.sf.l2jdev.gameserver.model.residences.AbstractResidence;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.residences.ClanHallTeleportHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ClanHallData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallData.class.getName());
	private final Map<Integer, ClanHall> _clanHalls = new ConcurrentHashMap<>();

	protected ClanHallData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackDirectory("data/residences/clanHalls", true);
		LOGGER.info(this.getClass().getSimpleName() + ": Succesfully loaded " + this._clanHalls.size() + " clan halls.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node listNode = document.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
		{
			if ("list".equals(listNode.getNodeName()))
			{
				for (Node clanHallNode = listNode.getFirstChild(); clanHallNode != null; clanHallNode = clanHallNode.getNextSibling())
				{
					if ("clanHall".equals(clanHallNode.getNodeName()))
					{
						List<Door> doors = new ArrayList<>();
						List<Integer> npcs = new ArrayList<>();
						List<ClanHallTeleportHolder> teleports = new ArrayList<>();
						StatSet params = new StatSet();
						params.set("id", this.parseInteger(clanHallNode.getAttributes(), "id"));
						params.set("name", this.parseString(clanHallNode.getAttributes(), "name", "None"));
						params.set("grade", this.parseEnum(clanHallNode.getAttributes(), ClanHallGrade.class, "grade", ClanHallGrade.GRADE_NONE));
						params.set("type", this.parseEnum(clanHallNode.getAttributes(), ClanHallType.class, "type", ClanHallType.OTHER));

						for (Node tpNode = clanHallNode.getFirstChild(); tpNode != null; tpNode = tpNode.getNextSibling())
						{
							String var10 = tpNode.getNodeName();
							switch (var10)
							{
								case "auction":
									NamedNodeMap at = tpNode.getAttributes();
									params.set("minBid", this.parseInteger(at, "minBid"));
									params.set("lease", this.parseInteger(at, "lease"));
									params.set("deposit", this.parseInteger(at, "deposit"));
									break;
								case "npcs":
									Node npcNode = tpNode.getFirstChild();

									for (; npcNode != null; npcNode = npcNode.getNextSibling())
									{
										if ("npc".equals(npcNode.getNodeName()))
										{
											NamedNodeMap np = npcNode.getAttributes();
											int npcId = this.parseInteger(np, "id");
											npcs.add(npcId);
										}
									}

									params.set("npcList", npcs);
									break;
								case "doorlist":
									for (Node npcNodex = tpNode.getFirstChild(); npcNodex != null; npcNodex = npcNodex.getNextSibling())
									{
										if ("door".equals(npcNodex.getNodeName()))
										{
											NamedNodeMap np = npcNodex.getAttributes();
											int doorId = this.parseInteger(np, "id");
											Door door = DoorData.getInstance().getDoor(doorId);
											if (door != null)
											{
												doors.add(door);
											}
										}
									}

									params.set("doorList", doors);
									break;
								case "teleportList":
									for (Node teleportNode = tpNode.getFirstChild(); teleportNode != null; teleportNode = teleportNode.getNextSibling())
									{
										if ("teleport".equals(teleportNode.getNodeName()))
										{
											NamedNodeMap np = teleportNode.getAttributes();
											int npcStringId = this.parseInteger(np, "npcStringId");
											int x = this.parseInteger(np, "x");
											int y = this.parseInteger(np, "y");
											int z = this.parseInteger(np, "z");
											int minFunctionLevel = this.parseInteger(np, "minFunctionLevel");
											int cost = this.parseInteger(np, "cost");
											teleports.add(new ClanHallTeleportHolder(npcStringId, x, y, z, minFunctionLevel, cost));
										}
									}

									params.set("teleportList", teleports);
									break;
								case "ownerRestartPoint":
									NamedNodeMap ol = tpNode.getAttributes();
									params.set("owner_loc", new Location(this.parseInteger(ol, "x"), this.parseInteger(ol, "y"), this.parseInteger(ol, "z")));
									break;
								case "banishPoint":
									NamedNodeMap bl = tpNode.getAttributes();
									params.set("banish_loc", new Location(this.parseInteger(bl, "x"), this.parseInteger(bl, "y"), this.parseInteger(bl, "z")));
							}
						}
						this._clanHalls.put(params.getInt("id"), new ClanHall(params));
					}
				}
			}
		}
	}

	public ClanHall getClanHallById(int clanHallId)
	{
		return this._clanHalls.get(clanHallId);
	}

	public Collection<ClanHall> getClanHalls()
	{
		return this._clanHalls.values();
	}

	public ClanHall getClanHallByNpcId(int npcId)
	{
		for (ClanHall ch : this._clanHalls.values())
		{
			if (ch.getNpcs().contains(npcId))
			{
				return ch;
			}
		}

		return null;
	}

	public ClanHall getClanHallByClan(Clan clan)
	{
		for (ClanHall ch : this._clanHalls.values())
		{
			if (ch.getOwner() == clan)
			{
				return ch;
			}
		}

		return null;
	}

	public ClanHall getClanHallByDoorId(int doorId)
	{
		Door door = DoorData.getInstance().getDoor(doorId);

		for (ClanHall ch : this._clanHalls.values())
		{
			if (ch.getDoors().contains(door))
			{
				return ch;
			}
		}

		return null;
	}

	public List<ClanHall> getFreeAuctionableHall()
	{
		List<ClanHall> freeAuctionableHalls = new ArrayList<>();

		for (ClanHall ch : this._clanHalls.values())
		{
			if (ch.getType() == ClanHallType.AUCTIONABLE && ch.getOwner() == null)
			{
				freeAuctionableHalls.add(ch);
			}
		}

		Collections.sort(freeAuctionableHalls, Comparator.comparingInt(AbstractResidence::getResidenceId));
		return freeAuctionableHalls;
	}

	public static ClanHallData getInstance()
	{
		return ClanHallData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ClanHallData INSTANCE = new ClanHallData();
	}
}
