package net.sf.l2jdev.gameserver.model.skill;

public enum SkillBuffType
{
	NONE,
	BUFF,
	DEBUFF,
	DANCE,
	TOGGLE,
	TRIGGER;

	public boolean isNone()
	{
		return this == NONE;
	}

	public boolean isBuff()
	{
		return this == BUFF;
	}

	public boolean isDebuff()
	{
		return this == DEBUFF;
	}

	public boolean isDance()
	{
		return this == DANCE;
	}

	public boolean isToggle()
	{
		return this == TOGGLE;
	}

	public boolean isTrigger()
	{
		return this == TRIGGER;
	}
}
