package net.sf.l2jdev.gameserver.model.actor.enums.creature;

import net.sf.l2jdev.gameserver.model.interfaces.ILocational;

public enum Position
{
	FRONT,
	SIDE,
	BACK;

	public static Position getPosition(ILocational from, ILocational to)
	{
		int heading = Math.abs(to.getHeading() - from.calculateHeadingTo(to));
		if ((heading < 8192 || heading > 24576) && Integer.toUnsignedLong(heading - 40960) > 16384L)
		{
			return Integer.toUnsignedLong(heading - 8192) <= 49152L ? FRONT : BACK;
		}
		return SIDE;
	}
}
