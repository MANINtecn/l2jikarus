package net.sf.l2jdev.gameserver.model.skill;

public class MountEnabledSkillList
{
	protected static final int STRIDER_SIEGE_ASSAULT = 325;
	protected static final int WYVERN_BREATH = 4289;

	public static boolean contains(int skillId)
	{
		return skillId == 325 || skillId == 4289;
	}
}
