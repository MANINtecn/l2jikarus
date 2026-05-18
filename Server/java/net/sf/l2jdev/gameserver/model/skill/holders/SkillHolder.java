package net.sf.l2jdev.gameserver.model.skill.holders;

import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class SkillHolder
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private Skill _skill;

	public SkillHolder(int skillId, int skillLevel)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._skillSubLevel = 0;
		this._skill = null;
	}

	public SkillHolder(int skillId, int skillLevel, int skillSubLevel)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._skillSubLevel = skillSubLevel;
		this._skill = null;
	}

	public SkillHolder(Skill skill)
	{
		this._skillId = skill.getId();
		this._skillLevel = skill.getLevel();
		this._skillSubLevel = skill.getSubLevel();
		this._skill = skill;
	}

	public int getSkillId()
	{
		return this._skillId;
	}

	public int getSkillLevel()
	{
		return this._skillLevel;
	}

	public int getSkillSubLevel()
	{
		return this._skillSubLevel;
	}

	public Skill getSkill()
	{
		if (this._skill == null)
		{
			this._skill = SkillData.getInstance().getSkill(this._skillId, Math.max(this._skillLevel, 1), this._skillSubLevel);
		}

		return this._skill;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		return !(obj instanceof SkillHolder holder) ? false : holder.getSkillId() == this._skillId && holder.getSkillLevel() == this._skillLevel && holder.getSkillSubLevel() == this._skillSubLevel;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		result = 31 * result + this._skillId;
		result = 31 * result + this._skillLevel;
		return 31 * result + this._skillSubLevel;
	}

	@Override
	public String toString()
	{
		return "[SkillId: " + this._skillId + " Level: " + this._skillLevel + "]";
	}
}
