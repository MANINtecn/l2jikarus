package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.ArmorSet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ArmorsetSkillHolder;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ArmorSetData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ArmorSetData.class.getName());
	private ArmorSet[] _armorSets;
	private final Map<Integer, ArmorSet> _armorSetMap = new HashMap<>();
	private List<ArmorSet>[] _itemSets;
	private final Map<Integer, List<ArmorSet>> _armorSetItems = new ConcurrentHashMap<>();

	protected ArmorSetData()
	{
		this.load();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load()
	{
		this.parseDatapackDirectory("data/stats/armorsets", false);
		this._armorSets = new ArmorSet[Collections.max(this._armorSetMap.keySet()) + 1];

		for (Entry<Integer, ArmorSet> armorSet : this._armorSetMap.entrySet())
		{
			this._armorSets[armorSet.getKey()] = armorSet.getValue();
		}

		_itemSets = new ArrayList[Collections.max(_armorSetItems.keySet()) + 1];

		for (Entry<Integer, List<ArmorSet>> armorSet : this._armorSetItems.entrySet())
		{
			this._itemSets[armorSet.getKey()] = armorSet.getValue();
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._armorSetMap.size() + " armor sets.");
		this._armorSetMap.clear();
		this._armorSetItems.clear();
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node setNode = n.getFirstChild(); setNode != null; setNode = setNode.getNextSibling())
				{
					if ("set".equalsIgnoreCase(setNode.getNodeName()))
					{
						int id = this.parseInteger(setNode.getAttributes(), "id");
						int minimumPieces = this.parseInteger(setNode.getAttributes(), "minimumPieces", 0);
						boolean isVisual = this.parseBoolean(setNode.getAttributes(), "visual", false);
						Set<Integer> requiredItems = new LinkedHashSet<>();
						Set<Integer> optionalItems = new LinkedHashSet<>();
						List<ArmorsetSkillHolder> skills = new ArrayList<>();
						Map<BaseStat, Double> stats = new LinkedHashMap<>();

						for (Node innerSetNode = setNode.getFirstChild(); innerSetNode != null; innerSetNode = innerSetNode.getNextSibling())
						{
							String var13 = innerSetNode.getNodeName();
							switch (var13)
							{
								case "requiredItems":
									this.forEach(innerSetNode, b -> "item".equals(b.getNodeName()), node -> {
										NamedNodeMap attrs = node.getAttributes();
										int itemId = this.parseInteger(attrs, "id");
										ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
										if (item == null)
										{
											LOGGER.warning("Attempting to register non existing required item: " + itemId + " to a set: " + file.getName());
										}
										else if (!requiredItems.add(itemId))
										{
											LOGGER.warning("Attempting to register duplicate required item " + item + " to a set: " + file.getName());
										}
									});
									break;
								case "optionalItems":
									this.forEach(innerSetNode, b -> "item".equals(b.getNodeName()), node -> {
										NamedNodeMap attrs = node.getAttributes();
										int itemId = this.parseInteger(attrs, "id");
										ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
										if (item == null)
										{
											LOGGER.warning("Attempting to register non existing optional item: " + itemId + " to a set: " + file.getName());
										}
										else if (!optionalItems.add(itemId))
										{
											LOGGER.warning("Attempting to register duplicate optional item " + item + " to a set: " + file.getName());
										}
									});
									break;
								case "skills":
									this.forEach(innerSetNode, b -> "skill".equals(b.getNodeName()), node -> {
										NamedNodeMap attrs = node.getAttributes();
										int skillId = this.parseInteger(attrs, "id");
										int skillLevel = this.parseInteger(attrs, "level");
										int minPieces = this.parseInteger(attrs, "minimumPieces", minimumPieces);
										int minEnchant = this.parseInteger(attrs, "minimumEnchant", 0);
										boolean isOptional = this.parseBoolean(attrs, "optional", false);
										int artifactSlotMask = this.parseInteger(attrs, "slotMask", 0);
										int artifactBookSlot = this.parseInteger(attrs, "bookSlot", 0);
										skills.add(new ArmorsetSkillHolder(skillId, skillLevel, minPieces, minEnchant, isOptional, artifactSlotMask, artifactBookSlot));
									});
									break;
								case "stats":
									this.forEach(innerSetNode, b -> "stat".equals(b.getNodeName()), node -> {
										NamedNodeMap attrs = node.getAttributes();
										stats.put(this.parseEnum(attrs, BaseStat.class, "type"), this.parseDouble(attrs, "val"));
									});
							}
						}

						ArmorSet set = new ArmorSet(id, minimumPieces, isVisual, requiredItems, optionalItems, skills, stats);
						if (this._armorSetMap.putIfAbsent(id, set) != null)
						{
							LOGGER.warning("Duplicate set entry with id: " + id + " in file: " + file.getName());
						}

						Stream.concat(Arrays.stream(set.getRequiredItems()).boxed(), Arrays.stream(set.getOptionalItems()).boxed()).forEach(itemHolder -> this._armorSetItems.computeIfAbsent(itemHolder, _ -> new ArrayList<>()).add(set));
					}
				}
			}
		}
	}

	public ArmorSet getSet(int setId)
	{
		return this._armorSets.length > setId ? this._armorSets[setId] : null;
	}

	public List<ArmorSet> getSets(int itemId)
	{
		if (this._itemSets.length > itemId)
		{
			List<ArmorSet> sets = this._itemSets[itemId];
			if (sets != null)
			{
				return sets;
			}
		}

		return Collections.emptyList();
	}

	public static ArmorSetData getInstance()
	{
		return ArmorSetData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ArmorSetData INSTANCE = new ArmorSetData();
	}
}
