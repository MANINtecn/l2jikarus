package net.sf.l2jdev.gameserver.model.options;

import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OptionSkillHolder
{
	private final Skill _skill;
	private final double _chance;
	private final OptionSkillType _type;

	public OptionSkillHolder(Skill skill, double chance, OptionSkillType type)
	{
		this._skill = skill;
		this._chance = chance;
		this._type = type;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public double getChance()
	{
		return this._chance;
	}

	public OptionSkillType getSkillType()
	{
		return this._type;
	}
}
