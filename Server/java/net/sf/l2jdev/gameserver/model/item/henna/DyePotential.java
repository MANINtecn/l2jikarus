package net.sf.l2jdev.gameserver.model.item.henna;

import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class DyePotential
{
	private final int _id;
	private final int _slotId;
	private final int _skillId;
	private final Skill[] _skills;
	private final int _maxSkillLevel;

	public DyePotential(int id, int slotId, int skillId, int maxSkillLevel)
	{
		this._id = id;
		this._slotId = slotId;
		this._skillId = skillId;
		this._skills = new Skill[maxSkillLevel];

		for (int i = 1; i <= maxSkillLevel; i++)
		{
			this._skills[i - 1] = SkillData.getInstance().getSkill(skillId, i);
		}

		this._maxSkillLevel = maxSkillLevel;
	}

	public int getId()
	{
		return this._id;
	}

	public int getSlotId()
	{
		return this._slotId;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public Skill getSkill(int level)
	{
		return this._skills[level - 1];
	}

	public int getMaxSkillLevel()
	{
		return this._maxSkillLevel;
	}
}
