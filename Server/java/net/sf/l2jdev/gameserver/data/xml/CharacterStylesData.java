package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.data.holders.CharacterStyleDataHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CharacterStylesData implements IXmlReader
{
	private final Map<CharacterStyleCategoryType, List<CharacterStyleDataHolder>> STYLES = new HashMap<>();
	private final Map<CharacterStyleCategoryType, ItemHolder> SWAP_COST_HOLDER = new HashMap<>();
	private final ConcurrentMap<Integer, Integer> _cachedWeaponMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Integer, SkillHolder> _cachedKillEffectMap = new ConcurrentHashMap<>();

	public CharacterStylesData()
	{
		this.load();
		this.buildCacheMaps();
	}

	@Override
	public void load()
	{
		this.STYLES.clear();
		this.SWAP_COST_HOLDER.clear();
		this.parseDatapackFile("data/CharacterStylesData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this.STYLES.size() + " Character Styles.");

		for (CharacterStyleCategoryType type : this.STYLES.keySet())
		{
			LOGGER.info(this.getClass().getSimpleName() + ":   " + type + " -> " + this.STYLES.get(type).size() + " styles.");
		}
	}

	public void loadDatapack(String relativePath)
	{
		this.parseDatapackFile(relativePath);
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		Node root = document.getDocumentElement();
		if (root != null)
		{
			this.forEach(root, "category", categoryNode -> {
				NamedNodeMap catAttr = categoryNode.getAttributes();
				String typeStr = this.parseString(catAttr, "type", null);
				if (typeStr != null && !typeStr.isEmpty())
				{
					CharacterStyleCategoryType type = CharacterStyleCategoryType.from(typeStr);
					List<CharacterStyleDataHolder> stylesList = this.STYLES.computeIfAbsent(type, _ -> new ArrayList<>());
					Integer styleCostId = this.parseInteger(catAttr, "swapCostId", 0);
					Long styleCostCount = this.parseLong(catAttr, "swapCostCount", 0L);
					if (styleCostId != null && styleCostCount != null && styleCostId > 0 && styleCostCount > 0L)
					{
						this.SWAP_COST_HOLDER.put(type, new ItemHolder(styleCostId, styleCostCount));
					}

					this.forEach(categoryNode, "style", styleNode -> {
						NamedNodeMap sAttr = styleNode.getAttributes();
						Integer styleId = this.parseInteger(sAttr, "styleId");
						if (styleId != null)
						{
							String name = this.parseString(sAttr, "name", "");
							Integer shiftWeaponId = this.parseInteger(sAttr, "shiftWeaponId", 0);
							WeaponType weaponType = this.parseEnum(sAttr, WeaponType.class, "weaponType", WeaponType.NONE);
							Integer skillId = this.parseInteger(sAttr, "skillId", 0);
							Integer skillLevel = this.parseInteger(sAttr, "skillLevel", 0);
							List<ItemHolder> cost = new ArrayList<>();
							this.forEach(styleNode, "cost", costNode -> this.forEach(costNode, "item", itemNode -> {
								NamedNodeMap iAttr = itemNode.getAttributes();
								Integer itemId = this.parseInteger(iAttr, "id");
								Long count = this.parseLong(iAttr, "count", 1L);
								if (itemId != null)
								{
									cost.add(new ItemHolder(itemId, count != null ? count : 1L));
								}
							}));
							CharacterStyleDataHolder holder = null;
							if (type == CharacterStyleCategoryType.APPEARANCE_WEAPON)
							{
								holder = new CharacterStyleDataHolder(styleId, name, shiftWeaponId, weaponType, cost);
							}
							else if (type == CharacterStyleCategoryType.KILL_EFFECT)
							{
								SkillHolder sHolder = new SkillHolder(skillId, skillLevel);
								holder = new CharacterStyleDataHolder(styleId, name, sHolder, cost);
							}
							else
							{
								holder = new CharacterStyleDataHolder(styleId, name, cost);
							}

							stylesList.add(holder);
						}
					});
				}
			});
		}
	}

	public void buildCacheMaps()
	{
		this._cachedWeaponMap.clear();
		this._cachedKillEffectMap.clear();

		for (CharacterStyleCategoryType styleType : CharacterStyleCategoryType.values())
		{
			List<CharacterStyleDataHolder> list = this.STYLES.get(styleType);
			if (list.size() > 0)
			{
				if (styleType == CharacterStyleCategoryType.APPEARANCE_WEAPON)
				{
					for (CharacterStyleDataHolder listEntry : list)
					{
						this._cachedWeaponMap.putIfAbsent(listEntry.getStyleId(), listEntry.getShiftWeaponId());
					}
				}
				else if (styleType == CharacterStyleCategoryType.KILL_EFFECT)
				{
					for (CharacterStyleDataHolder listEntry : list)
					{
						this._cachedKillEffectMap.putIfAbsent(listEntry.getStyleId(), listEntry.getSkillHolder());
					}
				}
			}
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Cached " + this._cachedWeaponMap.size() + " Weapon Styles.");
		LOGGER.info(this.getClass().getSimpleName() + ": Cached " + this._cachedKillEffectMap.size() + " Kill Effect Styles.");
	}

	public int getWeaponStyleByStyleId(int styleId)
	{
		return this._cachedWeaponMap.containsKey(styleId) ? this._cachedWeaponMap.get(styleId) : 0;
	}

	public SkillHolder getKillEffectStyleByStyleId(int styleId)
	{
		return this._cachedKillEffectMap.containsKey(styleId) ? this._cachedKillEffectMap.get(styleId) : null;
	}

	public List<CharacterStyleDataHolder> getStylesByCategory(CharacterStyleCategoryType category)
	{
		return this.STYLES.getOrDefault(category, Collections.emptyList());
	}

	public CharacterStyleDataHolder getSpecificStyleByCategoryAndId(CharacterStyleCategoryType category, int styleId)
	{
		for (CharacterStyleDataHolder holder : this.getStylesByCategory(category))
		{
			if (holder._styleId == styleId)
			{
				return holder;
			}
		}

		return null;
	}

	public ItemHolder getSwapCostItemByCategory(CharacterStyleCategoryType category)
	{
		return this.SWAP_COST_HOLDER.get(category);
	}

	public static CharacterStylesData getInstance()
	{
		return CharacterStylesData.Singleton.INSTANCE;
	}

	private static class Singleton
	{
		protected static final CharacterStylesData INSTANCE = new CharacterStylesData();
	}
}
