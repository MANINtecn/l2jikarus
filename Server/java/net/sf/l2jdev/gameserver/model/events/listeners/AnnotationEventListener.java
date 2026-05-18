package net.sf.l2jdev.gameserver.model.events.listeners;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;

public class AnnotationEventListener extends AbstractEventListener
{
	private static final Logger LOGGER = Logger.getLogger(AnnotationEventListener.class.getName());
	private final Method _callback;

	public AnnotationEventListener(ListenersContainer container, EventType type, Method callback, Object owner, int priority)
	{
		super(container, type, owner);
		this._callback = callback;
		this.setPriority(priority);
	}

	@Override
	public <R extends AbstractEventReturn> R executeEvent(IBaseEvent event, Class<R> returnBackClass)
	{
		try
		{
			Object result = this._callback.invoke(this.getOwner(), event);
			if (this._callback.getReturnType() == returnBackClass)
			{
				return returnBackClass.cast(result);
			}
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while invoking " + this._callback.getName() + " on " + this.getOwner(), var4);
		}

		return null;
	}
}
