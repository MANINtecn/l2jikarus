package net.sf.l2jdev.gameserver.model.interfaces;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.Position;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public interface ILocational
{
	int getX();

	int getY();

	int getZ();

	int getHeading();

	ILocational getLocation();

	default int calculateHeadingTo(ILocational to)
	{
		return LocationUtil.calculateHeadingFrom(this.getX(), this.getY(), to.getX(), to.getY());
	}

	default boolean isInFrontOf(ILocational target)
	{
		return target == null ? false : Position.FRONT == Position.getPosition(this, target);
	}

	default boolean isOnSideOf(ILocational target)
	{
		return target == null ? false : Position.SIDE == Position.getPosition(this, target);
	}

	default boolean isBehind(ILocational target)
	{
		return target == null ? false : Position.BACK == Position.getPosition(this, target);
	}
}
