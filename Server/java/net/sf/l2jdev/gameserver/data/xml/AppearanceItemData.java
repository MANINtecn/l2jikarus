package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceHolder;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceStone;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceTargetType;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AppearanceItemData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AppearanceItemData.class.getName());
	private AppearanceStone[] _stones;
	private final Map<Integer, AppearanceStone> _stoneMap = new HashMap<>();

	protected AppearanceItemData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("data/AppearanceStones.xml");
		if (!this._stoneMap.isEmpty())
		{
			this._stones = new AppearanceStone[Collections.max(this._stoneMap.keySet()) + 1];

			for (Entry<Integer, AppearanceStone> stone : this._stoneMap.entrySet())
			{
				this._stones[stone.getKey()] = stone.getValue();
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._stoneMap.size() + " stones.");
		}

		this._stoneMap.clear();
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
					if ("appearance_stone".equalsIgnoreCase(d.getNodeName()))
					{
						AppearanceStone stone = new AppearanceStone(new StatSet(this.parseAttributes(d)));

						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							String var7 = c.getNodeName();
							switch (var7)
							{
								case "grade":
								{
									CrystalType type = CrystalType.valueOf(c.getTextContent());
									stone.addCrystalType(type);
									break;
								}
								case "targetType":
								{
									AppearanceTargetType type = AppearanceTargetType.valueOf(c.getTextContent());
									stone.addTargetType(type);
									break;
								}
								case "bodyPart":
									BodyPart bodyPart = BodyPart.fromName(c.getTextContent());
									stone.addBodyPart(bodyPart);
									break;
								case "race":
									Race race = Race.valueOf(c.getTextContent());
									stone.addRace(race);
									break;
								case "raceNot":
									Race raceNot = Race.valueOf(c.getTextContent());
									stone.addRaceNot(raceNot);
									break;
								case "visual":
									stone.addVisualId(new AppearanceHolder(new StatSet(this.parseAttributes(c))));
							}
						}

						if (ItemData.getInstance().getTemplate(stone.getId()) != null)
						{
							this._stoneMap.put(stone.getId(), stone);
						}
						else
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find appearance stone item " + stone.getId());
						}
					}
				}
			}
		}
	}

	public AppearanceStone getStone(int stone)
	{
		return this._stones.length > stone ? this._stones[stone] : null;
	}

	public static AppearanceItemData getInstance()
	{
		return AppearanceItemData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AppearanceItemData INSTANCE = new AppearanceItemData();
	}
}
