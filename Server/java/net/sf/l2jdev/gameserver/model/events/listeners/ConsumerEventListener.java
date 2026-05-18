package net.sf.l2jdev.gameserver.model.events.listeners;

import java.util.function.Consumer;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;

public class ConsumerEventListener extends AbstractEventListener
{
	private final Consumer<Object> _callback;

	@SuppressWarnings("unchecked")
	public ConsumerEventListener(ListenersContainer container, EventType type, Consumer<?> callback, Object owner)
	{
		super(container, type, owner);
		this._callback = (Consumer<Object>) callback;
	}

	@Override
	public <R extends AbstractEventReturn> R executeEvent(IBaseEvent event, Class<R> returnBackClass)
	{
		this._callback.accept(event);
		return null;
	}
}
