package net.sf.l2jdev.gameserver.handler;

import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;

public class ActionHandler implements IHandler<IActionHandler, InstanceType>
{
	private final Map<InstanceType, IActionHandler> _actions = new EnumMap<>(InstanceType.class);

	protected ActionHandler()
	{
	}

	@Override
	public void registerHandler(IActionHandler handler)
	{
		this._actions.put(handler.getInstanceType(), handler);
	}

	@Override
	public synchronized void removeHandler(IActionHandler handler)
	{
		this._actions.remove(handler.getInstanceType());
	}

	@Override
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;

		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = this._actions.get(t);
			if (result != null)
			{
				break;
			}
		}

		return result;
	}

	@Override
	public int size()
	{
		return this._actions.size();
	}

	public static ActionHandler getInstance()
	{
		return ActionHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ActionHandler INSTANCE = new ActionHandler();
	}
}
