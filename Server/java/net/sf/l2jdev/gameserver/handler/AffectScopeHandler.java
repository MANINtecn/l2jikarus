package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;

public class AffectScopeHandler implements IHandler<IAffectScopeHandler, Enum<AffectScope>>
{
	private final Map<Enum<AffectScope>, IAffectScopeHandler> _datatable = new HashMap<>();

	protected AffectScopeHandler()
	{
	}

	@Override
	public void registerHandler(IAffectScopeHandler handler)
	{
		this._datatable.put(handler.getAffectScopeType(), handler);
	}

	@Override
	public synchronized void removeHandler(IAffectScopeHandler handler)
	{
		this._datatable.remove(handler.getAffectScopeType());
	}

	@Override
	public IAffectScopeHandler getHandler(Enum<AffectScope> affectScope)
	{
		return this._datatable.get(affectScope);
	}

	@Override
	public int size()
	{
		return this._datatable.size();
	}

	public static AffectScopeHandler getInstance()
	{
		return AffectScopeHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AffectScopeHandler INSTANCE = new AffectScopeHandler();
	}
}
