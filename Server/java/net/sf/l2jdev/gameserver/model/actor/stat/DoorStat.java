package net.sf.l2jdev.gameserver.model.actor.stat;

import net.sf.l2jdev.gameserver.model.actor.instance.Door;

public class DoorStat extends CreatureStat
{
	private int _upgradeHpRatio = 1;

	public DoorStat(Door activeChar)
	{
		super(activeChar);
	}

	@Override
	public Door getActiveChar()
	{
		return super.getActiveChar().asDoor();
	}

	@Override
	public long getMaxHp()
	{
		return super.getMaxHp() * this._upgradeHpRatio;
	}

	public void setUpgradeHpRatio(int ratio)
	{
		this._upgradeHpRatio = ratio;
	}

	public int getUpgradeHpRatio()
	{
		return this._upgradeHpRatio;
	}
}
