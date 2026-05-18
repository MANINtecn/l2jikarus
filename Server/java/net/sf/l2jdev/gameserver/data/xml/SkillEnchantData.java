package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.EnchantItemExpHolder;
import net.sf.l2jdev.gameserver.data.holders.EnchantStarHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillEnchantHolder;
import org.w3c.dom.Document;

public class SkillEnchantData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SkillEnchantData.class.getName());
	private final Map<Integer, EnchantStarHolder> _enchantStarMap = new HashMap<>();
	private final Map<Integer, SkillEnchantHolder> _skillEnchantMap = new ConcurrentHashMap<>();
	private final Map<Integer, Map<Integer, EnchantItemExpHolder>> _enchantItemMap = new HashMap<>();
	private final Map<Integer, Integer> _chanceEnchantMap = new HashMap<>();

	public SkillEnchantData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("data/SkillEnchantData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._enchantStarMap.size() + " star levels.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._enchantItemMap.size() + " enchant items.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._skillEnchantMap.size() + " skill enchants.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> {
			this.forEach(listNode, "skills", skills -> this.forEach(skills, "skill", skill -> {
				StatSet set = new StatSet(this.parseAttributes(skill));
				int id = set.getInt("id");
				int starLevel = set.getInt("starLevel");
				int maxEnchantLevel = set.getInt("maxEnchantLevel");
				this._skillEnchantMap.put(id, new SkillEnchantHolder(id, starLevel, maxEnchantLevel));
			}));
			this.forEach(listNode, "stars", stars -> this.forEach(stars, "star", star -> {
				StatSet set = new StatSet(this.parseAttributes(star));
				int level = set.getInt("level");
				EnchantStarHolder starHolder = new EnchantStarHolder(set);
				this._enchantStarMap.put(level, starHolder);
			}));
			this.forEach(listNode, "chances", itemsPoints -> this.forEach(itemsPoints, "chance", item -> {
				StatSet set = new StatSet(this.parseAttributes(item));
				int enchantLevel = set.getInt("enchantLevel");
				int chance = set.getInt("chance");
				this._chanceEnchantMap.put(enchantLevel, chance);
			}));
			this.forEach(listNode, "itemsPoints", itemsPoints -> this.forEach(itemsPoints, "star", star -> {
				StatSet set = new StatSet(this.parseAttributes(star));
				int level = set.getInt("level");
				Map<Integer, EnchantItemExpHolder> itemMap = new HashMap<>();
				this.forEach(star, "item", item -> {
					StatSet statSet = new StatSet(this.parseAttributes(item));
					int id = statSet.getInt("id");
					itemMap.put(id, new EnchantItemExpHolder(statSet));
				});
				this._enchantItemMap.put(level, itemMap);
			}));
		});
	}

	public EnchantStarHolder getEnchantStar(int level)
	{
		return this._enchantStarMap.get(level);
	}

	public SkillEnchantHolder getSkillEnchant(int id)
	{
		return this._skillEnchantMap.get(id);
	}

	public EnchantItemExpHolder getEnchantItem(int level, int id)
	{
		return this._enchantItemMap.get(level).get(id);
	}

	public Map<Integer, EnchantItemExpHolder> getEnchantItem(int level)
	{
		return this._enchantItemMap.get(level);
	}

	public void addReplacedSkillEnchant(int oldId, int newId)
	{
		if (this._skillEnchantMap.containsKey(oldId) && !this._skillEnchantMap.containsKey(newId))
		{
			SkillEnchantHolder oldHolder = this._skillEnchantMap.get(oldId);
			this._skillEnchantMap.put(newId, new SkillEnchantHolder(newId, oldHolder.getStarLevel(), oldHolder.getMaxEnchantLevel()));
		}
	}

	public int getChanceEnchantMap(Skill skill)
	{
		int enchantLevel = skill.getSubLevel() == 0 ? 1 : skill.getSubLevel() + 1 - 1000;
		return enchantLevel > this.getSkillEnchant(skill.getId()).getMaxEnchantLevel() ? 0 : this._chanceEnchantMap.get(enchantLevel);
	}

	public static SkillEnchantData getInstance()
	{
		return SkillEnchantData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SkillEnchantData INSTANCE = new SkillEnchantData();
	}
}
