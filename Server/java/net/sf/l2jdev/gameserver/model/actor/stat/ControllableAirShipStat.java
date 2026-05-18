package net.sf.l2jdev.gameserver.model.actor.stat;

import net.sf.l2jdev.gameserver.model.actor.instance.ControllableAirShip;

public class ControllableAirShipStat extends VehicleStat
{
	public ControllableAirShipStat(ControllableAirShip activeChar)
	{
		super(activeChar);
	}

	@Override
	public ControllableAirShip getActiveChar()
	{
		return (ControllableAirShip) super.getActiveChar();
	}

	@Override
	public double getMoveSpeed()
	{
		return !this.getActiveChar().isInDock() && this.getActiveChar().getFuel() <= 0 ? super.getMoveSpeed() * 0.05F : super.getMoveSpeed();
	}
}
