package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.EnchantSkillHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillEnchantType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;

public class EnchantSkillGroupsData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EnchantSkillGroupsData.class.getName());
	private final Map<Integer, EnchantSkillHolder> _enchantSkillHolders = new ConcurrentHashMap<>();
	private final Map<SkillHolder, Set<Integer>> _enchantSkillTrees = new ConcurrentHashMap<>();
	public static int MAX_ENCHANT_LEVEL;

	protected EnchantSkillGroupsData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._enchantSkillHolders.clear();
		this.parseDatapackFile("data/EnchantSkillGroups.xml");
		MAX_ENCHANT_LEVEL = this._enchantSkillHolders.size();
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._enchantSkillHolders.size() + " enchant routes, max enchant set to " + MAX_ENCHANT_LEVEL + ".");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "enchant", enchantNode -> {
			EnchantSkillHolder enchantSkillHolder = new EnchantSkillHolder(new StatSet(this.parseAttributes(enchantNode)));
			this.forEach(enchantNode, "sps", spsNode -> this.forEach(spsNode, "sp", spNode -> enchantSkillHolder.addSp(this.parseEnum(spNode.getAttributes(), SkillEnchantType.class, "type"), this.parseInteger(spNode.getAttributes(), "amount").intValue())));
			this.forEach(enchantNode, "chances", chancesNode -> this.forEach(chancesNode, "chance", chanceNode -> enchantSkillHolder.addChance(this.parseEnum(chanceNode.getAttributes(), SkillEnchantType.class, "type"), this.parseInteger(chanceNode.getAttributes(), "value"))));
			this.forEach(enchantNode, "items", itemsNode -> this.forEach(itemsNode, "item", itemNode -> enchantSkillHolder.addRequiredItem(this.parseEnum(itemNode.getAttributes(), SkillEnchantType.class, "type"), new ItemHolder(new StatSet(this.parseAttributes(itemNode))))));
			this._enchantSkillHolders.put(this.parseInteger(enchantNode.getAttributes(), "level"), enchantSkillHolder);
		}));
	}

	public void addRouteForSkill(int skillId, int level, int route)
	{
		this.addRouteForSkill(new SkillHolder(skillId, level), route);
	}

	public void addRouteForSkill(SkillHolder holder, int route)
	{
		this._enchantSkillTrees.computeIfAbsent(holder, _ -> new HashSet<>()).add(route);
	}

	public Set<Integer> getRouteForSkill(int skillId, int level)
	{
		return this.getRouteForSkill(skillId, level, 0);
	}

	public Set<Integer> getRouteForSkill(int skillId, int level, int subLevel)
	{
		return this.getRouteForSkill(new SkillHolder(skillId, level, subLevel));
	}

	public Set<Integer> getRouteForSkill(SkillHolder holder)
	{
		return this._enchantSkillTrees.getOrDefault(holder, Collections.emptySet());
	}

	public boolean isEnchantable(Skill skill)
	{
		return this.isEnchantable(new SkillHolder(skill.getId(), skill.getLevel()));
	}

	public boolean isEnchantable(SkillHolder holder)
	{
		return this._enchantSkillTrees.containsKey(holder);
	}

	public EnchantSkillHolder getEnchantSkillHolder(int level)
	{
		return this._enchantSkillHolders.getOrDefault(level, null);
	}

	public static EnchantSkillGroupsData getInstance()
	{
		return EnchantSkillGroupsData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EnchantSkillGroupsData INSTANCE = new EnchantSkillGroupsData();
	}
}
