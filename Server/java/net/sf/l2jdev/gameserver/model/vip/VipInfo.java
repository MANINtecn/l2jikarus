package net.sf.l2jdev.gameserver.model.vip;

public class VipInfo
{
	private final byte _tier;
	private final long _pointsRequired;
	private final long _pointsDepreciated;
	private int _skill;

	public VipInfo(byte tier, long pointsRequired, long pointsDepreciated)
	{
		this._tier = tier;
		this._pointsRequired = pointsRequired;
		this._pointsDepreciated = pointsDepreciated;
	}

	public byte getTier()
	{
		return this._tier;
	}

	public long getPointsRequired()
	{
		return this._pointsRequired;
	}

	public long getPointsDepreciated()
	{
		return this._pointsDepreciated;
	}

	public int getSkill()
	{
		return this._skill;
	}

	public void setSkill(int skill)
	{
		this._skill = skill;
	}
}
