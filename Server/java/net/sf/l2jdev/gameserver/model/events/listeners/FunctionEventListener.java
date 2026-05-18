package net.sf.l2jdev.gameserver.model.events.listeners;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;

public class FunctionEventListener extends AbstractEventListener
{
	private static final Logger LOGGER = Logger.getLogger(FunctionEventListener.class.getName());
	private final Function<Object, AbstractEventReturn> _callback;

	@SuppressWarnings("unchecked")
	public FunctionEventListener(ListenersContainer container, EventType type, Function<?, ?> callback, Object owner)
	{
		super(container, type, owner);
		this._callback = (Function<Object, AbstractEventReturn>) callback;
	}

	@Override
	public <R extends AbstractEventReturn> R executeEvent(IBaseEvent event, Class<R> returnBackClass)
	{
		try
		{
			return returnBackClass.cast(this._callback.apply(event));
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while invoking " + event + " on " + this.getOwner(), var4);
			return null;
		}
	}
}
