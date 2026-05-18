package net.sf.l2jdev.gameserver.data.holders;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillEnchantType;

public class EnchantSkillHolder
{
	private final int _level;
	private final int _enchantFailLevel;
	private final Map<SkillEnchantType, Long> _sp = new EnumMap<>(SkillEnchantType.class);
	private final Map<SkillEnchantType, Integer> _chance = new EnumMap<>(SkillEnchantType.class);
	private final Map<SkillEnchantType, Set<ItemHolder>> _requiredItems = new EnumMap<>(SkillEnchantType.class);

	public EnchantSkillHolder(StatSet set)
	{
		this._level = set.getInt("level");
		this._enchantFailLevel = set.getInt("enchantFailLevel");
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getEnchantFailLevel()
	{
		return this._enchantFailLevel;
	}

	public void addSp(SkillEnchantType type, long sp)
	{
		this._sp.put(type, sp);
	}

	public long getSp(SkillEnchantType type)
	{
		Long val = this._sp.get(type);
		return val != null ? val : 0L;
	}

	public void addChance(SkillEnchantType type, int chance)
	{
		this._chance.put(type, chance);
	}

	public int getChance(SkillEnchantType type)
	{
		Integer val = this._chance.get(type);
		return val != null ? val : 100;
	}

	public void addRequiredItem(SkillEnchantType type, ItemHolder item)
	{
		this._requiredItems.computeIfAbsent(type, _ -> new HashSet<>()).add(item);
	}

	public Set<ItemHolder> getRequiredItems(SkillEnchantType type)
	{
		return this._requiredItems.getOrDefault(type, Collections.emptySet());
	}
}
