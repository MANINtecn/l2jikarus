package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class ItemSkillHolder extends SkillHolder
{
	private final ItemSkillType _type;
	private final int _chance;
	private final int _value;

	public ItemSkillHolder(int skillId, int skillLevel, int skillSubLevel, ItemSkillType type, int chance, int value)
	{
		super(skillId, skillLevel, skillSubLevel);
		this._type = type;
		this._chance = chance;
		this._value = value;
	}

	public ItemSkillType getType()
	{
		return this._type;
	}

	public int getChance()
	{
		return this._chance;
	}

	public int getValue()
	{
		return this._value;
	}
}
