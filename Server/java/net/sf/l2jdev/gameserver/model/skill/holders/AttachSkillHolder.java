package net.sf.l2jdev.gameserver.model.skill.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class AttachSkillHolder extends SkillHolder
{
	private final int _requiredSkillId;
	private final int _requiredSkillLevel;

	public AttachSkillHolder(int skillId, int skillLevel, int requiredSkillId, int requiredSkillLevel)
	{
		super(skillId, skillLevel);
		this._requiredSkillId = requiredSkillId;
		this._requiredSkillLevel = requiredSkillLevel;
	}

	public int getRequiredSkillId()
	{
		return this._requiredSkillId;
	}

	public int getRequiredSkillLevel()
	{
		return this._requiredSkillLevel;
	}

	public static AttachSkillHolder fromStatSet(StatSet set)
	{
		return new AttachSkillHolder(set.getInt("skillId"), set.getInt("skillLevel", 1), set.getInt("requiredSkillId"), set.getInt("requiredSkillLevel", 1));
	}
}
