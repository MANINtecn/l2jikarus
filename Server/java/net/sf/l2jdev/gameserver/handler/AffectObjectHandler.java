package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.skill.targets.AffectObject;

public class AffectObjectHandler implements IHandler<IAffectObjectHandler, Enum<AffectObject>>
{
	private final Map<Enum<AffectObject>, IAffectObjectHandler> _datatable = new HashMap<>();

	protected AffectObjectHandler()
	{
	}

	@Override
	public void registerHandler(IAffectObjectHandler handler)
	{
		this._datatable.put(handler.getAffectObjectType(), handler);
	}

	@Override
	public synchronized void removeHandler(IAffectObjectHandler handler)
	{
		this._datatable.remove(handler.getAffectObjectType());
	}

	@Override
	public IAffectObjectHandler getHandler(Enum<AffectObject> targetType)
	{
		return this._datatable.get(targetType);
	}

	@Override
	public int size()
	{
		return this._datatable.size();
	}

	public static AffectObjectHandler getInstance()
	{
		return AffectObjectHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AffectObjectHandler INSTANCE = new AffectObjectHandler();
	}
}
