package net.sf.l2jdev.gameserver.model.clan;

import net.sf.l2jdev.gameserver.model.clan.enums.ClanRewardType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class ClanRewardBonus
{
	private final ClanRewardType _type;
	private final int _level;
	private final int _requiredAmount;
	private SkillHolder _skillReward;

	public ClanRewardBonus(ClanRewardType type, int level, int requiredAmount)
	{
		this._type = type;
		this._level = level;
		this._requiredAmount = requiredAmount;
	}

	public ClanRewardType getType()
	{
		return this._type;
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getRequiredAmount()
	{
		return this._requiredAmount;
	}

	public SkillHolder getSkillReward()
	{
		return this._skillReward;
	}

	public void setSkillReward(SkillHolder skillReward)
	{
		this._skillReward = skillReward;
	}
}
