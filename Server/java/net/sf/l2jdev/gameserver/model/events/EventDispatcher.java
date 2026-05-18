package net.sf.l2jdev.gameserver.model.events;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.events.listeners.AbstractEventListener;
import net.sf.l2jdev.gameserver.model.events.returns.AbstractEventReturn;

public class EventDispatcher
{
	private static final Logger LOGGER = Logger.getLogger(EventDispatcher.class.getName());

	protected EventDispatcher()
	{
	}

	public boolean hasListener(EventType type)
	{
		return Containers.Global().hasListener(type);
	}

	public boolean hasListener(EventType type, ListenersContainer container)
	{
		return Containers.Global().hasListener(type) || container != null && container.hasListener(type);
	}

	public boolean hasListener(EventType type, ListenersContainer... containers)
	{
		boolean hasListeners = Containers.Global().hasListener(type);
		if (!hasListeners)
		{
			for (ListenersContainer container : containers)
			{
				if (container.hasListener(type))
				{
					hasListeners = true;
					break;
				}
			}
		}

		return hasListeners;
	}

	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event)
	{
		return this.notifyEvent(event, null, null);
	}

	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event, Class<T> callbackClass)
	{
		return this.notifyEvent(event, null, callbackClass);
	}

	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event, ListenersContainer container)
	{
		return this.notifyEvent(event, container, null);
	}

	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event, ListenersContainer container, Class<T> callbackClass)
	{
		try
		{
			return this.notifyEventImpl(event, container, callbackClass);
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't notify event " + event.getClass().getSimpleName(), var5);
			return null;
		}
	}

	public void notifyEventAsync(IBaseEvent event, ListenersContainer container)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		ThreadPool.execute(() -> this.notifyEventToSingleContainer(event, container, null));
	}

	public void notifyEventAsync(IBaseEvent event, ListenersContainer... containers)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		ThreadPool.execute(() -> this.notifyEventToMultipleContainers(event, containers, null));
	}

	private <T extends AbstractEventReturn> T notifyEventToSingleContainer(IBaseEvent event, ListenersContainer container, Class<T> callbackClass)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		try
		{
			T callback = null;
			if (container != null)
			{
				callback = this.notifyToListeners(container.getListeners(event.getType()), event, callbackClass, callback);
			}

			if (callback == null || !callback.abort())
			{
				callback = this.notifyToListeners(Containers.Global().getListeners(event.getType()), event, callbackClass, callback);
			}

			return callback;
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't notify event " + event.getClass().getSimpleName(), var5);
			return null;
		}
	}

	private <T extends AbstractEventReturn> T notifyEventToMultipleContainers(IBaseEvent event, ListenersContainer[] containers, Class<T> callbackClass)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		try
		{
			T callback = null;
			if (containers != null)
			{
				for (ListenersContainer container : containers)
				{
					if (callback == null || !callback.abort())
					{
						callback = this.notifyToListeners(container.getListeners(event.getType()), event, callbackClass, callback);
					}
				}
			}

			if (callback == null || !callback.abort())
			{
				callback = this.notifyToListeners(Containers.Global().getListeners(event.getType()), event, callbackClass, callback);
			}

			return callback;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't notify event " + event.getClass().getSimpleName(), var9);
			return null;
		}
	}

	private <T extends AbstractEventReturn> T notifyEventImpl(IBaseEvent event, ListenersContainer container, Class<T> callbackClass)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		T callback = null;
		if (container != null)
		{
			callback = this.notifyToListeners(container.getListeners(event.getType()), event, callbackClass, callback);
		}

		if (callback == null || !callback.abort())
		{
			callback = this.notifyToListeners(Containers.Global().getListeners(event.getType()), event, callbackClass, callback);
		}

		return callback;
	}

	private <T extends AbstractEventReturn> T notifyToListeners(Collection<AbstractEventListener> listeners, IBaseEvent event, Class<T> returnBackClass, T callbackValue)
	{
		T callback = callbackValue;

		for (AbstractEventListener listener : listeners)
		{
			try
			{
				T rb = listener.executeEvent(event, returnBackClass);
				if (rb != null)
				{
					if (callback != null && !rb.override())
					{
						if (rb.abort())
						{
							break;
						}
					}
					else
					{
						callback = rb;
					}
				}
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception during notification of event: " + event.getClass().getSimpleName() + " listener: " + listener.getClass().getSimpleName(), var9);
			}
		}

		return callback;
	}

	public static EventDispatcher getInstance()
	{
		return EventDispatcher.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EventDispatcher INSTANCE = new EventDispatcher();
	}
}
