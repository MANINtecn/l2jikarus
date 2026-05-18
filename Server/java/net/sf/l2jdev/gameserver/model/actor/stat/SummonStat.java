package net.sf.l2jdev.gameserver.model.actor.stat;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Summon;

public class SummonStat extends PlayableStat
{
	public SummonStat(Summon activeChar)
	{
		super(activeChar);
	}

	@Override
	public Summon getActiveChar()
	{
		return super.getActiveChar().asSummon();
	}

	@Override
	public double getRunSpeed()
	{
		double val = super.getRunSpeed() + PlayerConfig.RUN_SPD_BOOST;
		return val > PlayerConfig.MAX_RUN_SPEED_SUMMON ? PlayerConfig.MAX_RUN_SPEED_SUMMON : val;
	}

	@Override
	public double getWalkSpeed()
	{
		double val = super.getWalkSpeed() + PlayerConfig.RUN_SPD_BOOST;
		return val > PlayerConfig.MAX_RUN_SPEED_SUMMON ? PlayerConfig.MAX_RUN_SPEED_SUMMON : val;
	}
}
