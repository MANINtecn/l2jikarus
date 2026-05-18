package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.henna.DyePotential;
import net.sf.l2jdev.gameserver.model.item.henna.DyePotentialFee;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HennaPatternPotentialData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HennaPatternPotentialData.class.getName());
	private final Map<Integer, Integer> _potenExpTable = new HashMap<>();
	private final Map<Integer, DyePotentialFee> _potenFees = new HashMap<>();
	private final Map<Integer, DyePotential> _potentials = new HashMap<>();
	private final List<ItemHolder> _enchancedReset = new ArrayList<>();
	private int MAX_POTEN_LEVEL = 0;
	private int MAX_POTEN_EXP = 0;

	protected HennaPatternPotentialData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._potenFees.clear();
		this._potenExpTable.clear();
		this._potentials.clear();
		this._enchancedReset.clear();
		this.parseDatapackFile("data/stats/hennaPatternPotential.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._potenFees.size() + " dye pattern fee data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node m = document.getFirstChild(); m != null; m = m.getNextSibling())
		{
			if ("list".equals(m.getNodeName()))
			{
				for (Node k = m.getFirstChild(); k != null; k = k.getNextSibling())
				{
					String var5 = k.getNodeName();
					switch (var5)
					{
						case "enchantFees":
							Node n = k.getFirstChild();

							for (; n != null; n = n.getNextSibling())
							{
								if ("fee".equals(n.getNodeName()))
								{
									NamedNodeMap attrs = n.getAttributes();
									StatSet set = new StatSet();

									for (int i = 0; i < attrs.getLength(); i++)
									{
										Node att = attrs.item(i);
										set.set(att.getNodeName(), att.getNodeValue());
									}

									int step = this.parseInteger(attrs, "step");
									int itemId = 0;
									long itemCount = 0L;
									long adenaFee = 0L;
									int dailyCount = 0;
									Map<Integer, Double> enchantExp = new HashMap<>();
									List<ItemHolder> items = new ArrayList<>();

									for (Node b = n.getFirstChild(); b != null; b = b.getNextSibling())
									{
										attrs = b.getAttributes();
										String var21 = b.getNodeName();
										switch (var21)
										{
											case "requiredItem":
												itemId = this.parseInteger(attrs, "id");
												itemCount = this.parseLong(attrs, "count", 1L);
												adenaFee = this.parseLong(attrs, "adenaFee", 0L);
												items.add(new ItemHolder(itemId, itemCount));
												break;
											case "dailyCount":
												dailyCount = Integer.parseInt(b.getTextContent());
												break;
											case "enchantExp":
												enchantExp.put(this.parseInteger(attrs, "count"), this.parseDouble(attrs, "chance"));
										}
									}

									this._potenFees.put(step, new DyePotentialFee(step, items, adenaFee, dailyCount, enchantExp));
								}
							}
							break;
						case "resetCount":
							for (Node resetNode = k.getFirstChild(); resetNode != null; resetNode = resetNode.getNextSibling())
							{
								if ("reset".equalsIgnoreCase(resetNode.getNodeName()))
								{
									StatSet set = new StatSet(this.parseAttributes(resetNode));
									int itemId = set.getInt("itemid");
									int itemCount = set.getInt("count");
									if (ItemData.getInstance().getTemplate(itemId) == null)
									{
										LOGGER.info(this.getClass().getSimpleName() + ": Item with id " + itemId + " does not exist.");
									}
									else
									{
										this._enchancedReset.add(new ItemHolder(itemId, itemCount));
									}
								}
							}
							break;
						case "experiencePoints":
							for (Node nxx = k.getFirstChild(); nxx != null; nxx = nxx.getNextSibling())
							{
								if ("hiddenPower".equals(nxx.getNodeName()))
								{
									NamedNodeMap attrs = nxx.getAttributes();
									StatSet set = new StatSet();

									for (int i = 0; i < attrs.getLength(); i++)
									{
										Node att = attrs.item(i);
										set.set(att.getNodeName(), att.getNodeValue());
									}

									int level = this.parseInteger(attrs, "level");
									int exp = this.parseInteger(attrs, "exp");
									this._potenExpTable.put(level, exp);
									if (this.MAX_POTEN_LEVEL < level)
									{
										this.MAX_POTEN_LEVEL = level;
									}

									if (this.MAX_POTEN_EXP < exp)
									{
										this.MAX_POTEN_EXP = exp;
									}
								}
							}
							break;
						case "hiddenPotentials":
							for (Node nx = k.getFirstChild(); nx != null; nx = nx.getNextSibling())
							{
								if ("poten".equals(nx.getNodeName()))
								{
									NamedNodeMap attrs = nx.getAttributes();
									StatSet set = new StatSet();

									for (int i = 0; i < attrs.getLength(); i++)
									{
										Node att = attrs.item(i);
										set.set(att.getNodeName(), att.getNodeValue());
									}

									int id = this.parseInteger(attrs, "id");
									int slotId = this.parseInteger(attrs, "slotId");
									int maxSkillLevel = this.parseInteger(attrs, "maxSkillLevel");
									int skillId = this.parseInteger(attrs, "skillId");
									this._potentials.put(id, new DyePotential(id, slotId, skillId, maxSkillLevel));
								}
							}
					}
				}
			}
		}
	}

	public DyePotentialFee getFee(int step)
	{
		return this._potenFees.get(step);
	}

	public int getMaxPotenEnchantStep()
	{
		return this._potenFees.size();
	}

	public List<ItemHolder> getEnchantReset()
	{
		return this._enchancedReset;
	}

	public int getExpForLevel(int level)
	{
		return this._potenExpTable.get(level);
	}

	public int getMaxPotenLevel()
	{
		return this.MAX_POTEN_LEVEL;
	}

	public int getMaxPotenExp()
	{
		return this.MAX_POTEN_EXP;
	}

	public DyePotential getPotential(int potenId)
	{
		return this._potentials.get(potenId);
	}

	public Skill getPotentialSkill(int potenId, int slotId, int level)
	{
		DyePotential potential = this._potentials.get(potenId);
		if (potential == null)
		{
			return null;
		}
		return potential.getSlotId() == slotId ? potential.getSkill(level) : null;
	}

	public Collection<Integer> getSkillIdsBySlotId(int slotId)
	{
		List<Integer> skillIds = new ArrayList<>();

		for (DyePotential potential : this._potentials.values())
		{
			if (potential.getSlotId() == slotId)
			{
				skillIds.add(potential.getSkillId());
			}
		}

		return skillIds;
	}

	public static HennaPatternPotentialData getInstance()
	{
		return HennaPatternPotentialData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final HennaPatternPotentialData INSTANCE = new HennaPatternPotentialData();
	}
}
