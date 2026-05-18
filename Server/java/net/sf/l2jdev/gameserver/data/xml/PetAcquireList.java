package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.holders.PetSkillAcquireHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PetAcquireList implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PetAcquireList.class.getName());
	private final Map<Integer, List<PetSkillAcquireHolder>> _skills = new HashMap<>();

	protected PetAcquireList()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._skills.clear();
		this.parseDatapackFile("data/PetAcquireList.xml");
		if (!this._skills.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._skills.size() + " pet skills.");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": System is disabled.");
		}
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
					if ("pet".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						StatSet set = new StatSet();

						for (int i = 0; i < attrs.getLength(); i++)
						{
							Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}

						int type = this.parseInteger(attrs, "type");
						List<PetSkillAcquireHolder> list = new ArrayList<>();

						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							attrs = b.getAttributes();
							if ("skill".equalsIgnoreCase(b.getNodeName()))
							{
								int skillId = this.parseInteger(attrs, "id");
								int skillLvl = this.parseInteger(attrs, "lvl");
								int reqLvl = this.parseInteger(attrs, "reqLvl");
								int evolve = this.parseInteger(attrs, "evolve");
								List<ItemHolder> items = new ArrayList<>();
								String itemAttr = this.parseString(attrs, "item");
								String itemAmountAttr = this.parseString(attrs, "itemAmount");
								if (itemAttr != null && itemAmountAttr != null)
								{
									String[] itemIds = itemAttr.split(";");
									String[] itemAmounts = itemAmountAttr.split(";");
									if (itemIds.length == itemAmounts.length)
									{
										for (int i = 0; i < itemIds.length; i++)
										{
											int itemId = Integer.parseInt(itemIds[i].trim());
											long itemAmount = Long.parseLong(itemAmounts[i].trim());
											items.add(new ItemHolder(itemId, itemAmount));
										}
									}
									else
									{
										LOGGER.warning("Mismatch in item and itemAmount counts for skill ID: " + skillId);
									}
								}

								list.add(new PetSkillAcquireHolder(skillId, skillLvl, reqLvl, evolve, items));
							}
						}

						this._skills.put(type, list);
					}
				}
			}
		}
	}

	public List<PetSkillAcquireHolder> getSkills(int type)
	{
		return this._skills.get(type);
	}

	public Map<Integer, List<PetSkillAcquireHolder>> getAllSkills()
	{
		return this._skills;
	}

	public int getSpecialSkillByType(int petType)
	{
		switch (petType)
		{
			case 12:
				return 49021;
			case 13:
				return 49031;
			case 14:
				return 49011;
			case 15:
				return 49001;
			case 16:
				return 49051;
			case 17:
				return 49041;
			default:
				throw new IllegalStateException("Unexpected value: " + petType);
		}
	}

	public static PetAcquireList getInstance()
	{
		return PetAcquireList.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PetAcquireList INSTANCE = new PetAcquireList();
	}
}
