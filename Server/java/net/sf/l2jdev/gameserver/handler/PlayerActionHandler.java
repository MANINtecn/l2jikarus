package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

public class PlayerActionHandler implements IHandler<IPlayerActionHandler, String>
{
	private final Map<String, IPlayerActionHandler> _actions = new HashMap<>();

	protected PlayerActionHandler()
	{
	}

	@Override
	public void registerHandler(IPlayerActionHandler handler)
	{
		this._actions.put(handler.getClass().getSimpleName(), handler);
	}

	@Override
	public synchronized void removeHandler(IPlayerActionHandler handler)
	{
		this._actions.remove(handler.getClass().getSimpleName());
	}

	@Override
	public IPlayerActionHandler getHandler(String name)
	{
		return this._actions.get(name);
	}

	@Override
	public int size()
	{
		return this._actions.size();
	}

	public static PlayerActionHandler getInstance()
	{
		return PlayerActionHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PlayerActionHandler INSTANCE = new PlayerActionHandler();
	}
}
