package net.sf.l2jdev.gameserver.model.events.holders;

import net.sf.l2jdev.gameserver.model.events.EventType;

public class OnDailyReset implements IBaseEvent
{
	@Override
	public EventType getType()
	{
		return EventType.ON_DAILY_RESET;
	}
}
