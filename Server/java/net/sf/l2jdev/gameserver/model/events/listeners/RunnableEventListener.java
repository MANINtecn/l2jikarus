package net.sf.l2jdev.gameserver.model.events.listeners;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;

public class RunnableEventListener extends AbstractEventListener
{
	private final Runnable _callback;

	public RunnableEventListener(ListenersContainer container, EventType type, Runnable callback, Object owner)
	{
		super(container, type, owner);
		this._callback = callback;
	}

	@Override
	public <R extends AbstractEventReturn> R executeEvent(IBaseEvent event, Class<R> returnBackClass)
	{
		this._callback.run();
		return null;
	}
}
