package net.sf.l2jdev.gameserver.model.events.holders;

import net.sf.l2jdev.gameserver.model.events.EventType;

public class OnServerStart implements IBaseEvent
{
	@Override
	public EventType getType()
	{
		return EventType.ON_SERVER_START;
	}
}
