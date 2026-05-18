package net.sf.l2jdev.gameserver.model.skill;

public enum SkillOperateType
{
	A1,
	A2,
	A3,
	A4,
	A5,
	A6,
	CA1,
	CA2,
	CA5,
	DA1,
	DA2,
	DA3,
	DA4,
	DA5,
	DA6,
	P,
	T,
	TG,
	AU;

	public boolean isActive()
	{
		switch (this)
		{
			case A1:
			case A2:
			case A3:
			case A4:
			case A5:
			case A6:
			case CA1:
			case CA5:
			case DA1:
			case DA2:
			case DA4:
			case DA5:
			case DA6:
				return true;
			case CA2:
			case DA3:
			default:
				return false;
		}
	}

	public boolean isContinuous()
	{
		switch (this)
		{
			case A2:
			case A3:
			case A4:
			case A5:
			case A6:
			case DA2:
			case DA4:
			case DA5:
			case DA6:
				return true;
			case CA1:
			case CA2:
			case CA5:
			case DA1:
			case DA3:
			default:
				return false;
		}
	}

	public boolean isSelfContinuous()
	{
		return this == A3;
	}

	public boolean isPassive()
	{
		return this == P;
	}

	public boolean isToggle()
	{
		return this == T || this == TG || this == AU;
	}

	public boolean isAura()
	{
		return this == A5 || this == A6 || this == AU;
	}

	public boolean isHidingMessages()
	{
		return this == P || this == A5 || this == A6 || this == TG;
	}

	public boolean isNotBroadcastable()
	{
		return this == AU || this == A5 || this == A6 || this == TG || this == T;
	}

	public boolean isChanneling()
	{
		switch (this)
		{
			case CA1:
			case CA2:
			case CA5:
				return true;
			default:
				return false;
		}
	}

	public boolean isSynergy()
	{
		return this == A6;
	}

	public boolean isFlyType()
	{
		switch (this)
		{
			case DA1:
			case DA2:
			case DA3:
			case DA4:
			case DA5:
				return true;
			default:
				return false;
		}
	}
}
