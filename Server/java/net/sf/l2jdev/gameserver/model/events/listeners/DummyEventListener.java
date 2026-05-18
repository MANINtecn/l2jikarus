package net.sf.l2jdev.gameserver.model.events.listeners;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;

public class DummyEventListener extends AbstractEventListener
{
	public DummyEventListener(ListenersContainer container, EventType type, Object owner)
	{
		super(container, type, owner);
	}

	@Override
	public <R extends AbstractEventReturn> R executeEvent(IBaseEvent event, Class<R> returnBackClass)
	{
		return null;
	}
}
