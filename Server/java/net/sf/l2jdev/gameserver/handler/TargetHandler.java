package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;

public class TargetHandler implements IHandler<ITargetTypeHandler, Enum<TargetType>>
{
	private final Map<Enum<TargetType>, ITargetTypeHandler> _datatable = new HashMap<>();

	protected TargetHandler()
	{
	}

	@Override
	public void registerHandler(ITargetTypeHandler handler)
	{
		this._datatable.put(handler.getTargetType(), handler);
	}

	@Override
	public synchronized void removeHandler(ITargetTypeHandler handler)
	{
		this._datatable.remove(handler.getTargetType());
	}

	@Override
	public ITargetTypeHandler getHandler(Enum<TargetType> targetType)
	{
		return this._datatable.get(targetType);
	}

	@Override
	public int size()
	{
		return this._datatable.size();
	}

	public static TargetHandler getInstance()
	{
		return TargetHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final TargetHandler INSTANCE = new TargetHandler();
	}
}
