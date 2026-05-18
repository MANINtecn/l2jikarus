package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.siege.CastleSide;
import net.sf.l2jdev.gameserver.model.siege.CastleSpawnHolder;
import net.sf.l2jdev.gameserver.model.siege.SiegeGuardHolder;
import net.sf.l2jdev.gameserver.model.siege.SiegeGuardType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CastleData implements IXmlReader
{
	private final Map<Integer, List<CastleSpawnHolder>> _spawns = new ConcurrentHashMap<>();
	private final Map<Integer, List<SiegeGuardHolder>> _siegeGuards = new ConcurrentHashMap<>();
	private static final Map<Integer, List<SkillHolder>> skills = new ConcurrentHashMap<>();

	protected CastleData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._spawns.clear();
		this._siegeGuards.clear();
		this.parseDatapackDirectory("data/residences/castles", true);
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node listNode = document.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
		{
			if ("list".equals(listNode.getNodeName()))
			{
				for (Node castleNode = listNode.getFirstChild(); castleNode != null; castleNode = castleNode.getNextSibling())
				{
					if ("castle".equals(castleNode.getNodeName()))
					{
						int castleId = this.parseInteger(castleNode.getAttributes(), "id");

						for (Node tpNode = castleNode.getFirstChild(); tpNode != null; tpNode = tpNode.getNextSibling())
						{
							List<CastleSpawnHolder> spawns = new ArrayList<>();
							if ("spawns".equals(tpNode.getNodeName()))
							{
								for (Node npcNode = tpNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling())
								{
									if ("npc".equals(npcNode.getNodeName()))
									{
										NamedNodeMap np = npcNode.getAttributes();
										int npcId = this.parseInteger(np, "id");
										CastleSide side = this.parseEnum(np, CastleSide.class, "castleSide", CastleSide.NEUTRAL);
										int x = this.parseInteger(np, "x");
										int y = this.parseInteger(np, "y");
										int z = this.parseInteger(np, "z");
										int heading = this.parseInteger(np, "heading");
										spawns.add(new CastleSpawnHolder(npcId, side, x, y, z, heading));
									}
								}

								this._spawns.put(castleId, spawns);
							}
							else if (!"siegeGuards".equals(tpNode.getNodeName()))
							{
								if ("skills".equalsIgnoreCase(tpNode.getNodeName()))
								{
									List<SkillHolder> list = new ArrayList<>();

									for (Node npcNodex = tpNode.getFirstChild(); npcNodex != null; npcNodex = npcNodex.getNextSibling())
									{
										if ("skill".equals(npcNodex.getNodeName()))
										{
											NamedNodeMap np = npcNodex.getAttributes();
											int id = this.parseInteger(np, "id");
											int lvl = this.parseInteger(np, "lvl");
											list.add(new SkillHolder(id, lvl));
										}

										skills.put(castleId, list);
									}
								}
							}
							else
							{
								List<SiegeGuardHolder> guards = new ArrayList<>();

								for (Node npcNodex = tpNode.getFirstChild(); npcNodex != null; npcNodex = npcNodex.getNextSibling())
								{
									if ("guard".equals(npcNodex.getNodeName()))
									{
										NamedNodeMap np = npcNodex.getAttributes();
										int itemId = this.parseInteger(np, "itemId");
										SiegeGuardType type = this.parseEnum(tpNode.getAttributes(), SiegeGuardType.class, "type");
										boolean stationary = this.parseBoolean(np, "stationary", false);
										int npcId = this.parseInteger(np, "npcId");
										int npcMaxAmount = this.parseInteger(np, "npcMaxAmount");
										guards.add(new SiegeGuardHolder(castleId, itemId, type, stationary, npcId, npcMaxAmount));
									}
								}

								this._siegeGuards.put(castleId, guards);
							}
						}
					}
				}
			}
		}
	}

	public List<CastleSpawnHolder> getSpawnsForSide(int castleId, CastleSide side)
	{
		List<CastleSpawnHolder> result = new ArrayList<>();
		if (this._spawns.containsKey(castleId))
		{
			for (CastleSpawnHolder spawn : this._spawns.get(castleId))
			{
				if (spawn.getSide() == side)
				{
					result.add(spawn);
				}
			}
		}

		return result;
	}

	public List<SiegeGuardHolder> getSiegeGuardsForCastle(int castleId)
	{
		return this._siegeGuards.getOrDefault(castleId, Collections.emptyList());
	}

	public Map<Integer, List<SiegeGuardHolder>> getSiegeGuards()
	{
		return this._siegeGuards;
	}

	public static Map<Integer, List<SkillHolder>> getSkills()
	{
		return skills;
	}

	public static CastleData getInstance()
	{
		return CastleData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CastleData INSTANCE = new CastleData();
	}
}
