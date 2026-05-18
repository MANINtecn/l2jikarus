package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.scripting.ScriptEngine;

public class EffectHandler
{
	private final Map<String, Function<StatSet, AbstractEffect>> _effectHandlerFactories = new HashMap<>();

	public void registerHandler(String name, Function<StatSet, AbstractEffect> handlerFactory)
	{
		this._effectHandlerFactories.put(name, handlerFactory);
	}

	public Function<StatSet, AbstractEffect> getHandlerFactory(String name)
	{
		return this._effectHandlerFactories.get(name);
	}

	public int size()
	{
		return this._effectHandlerFactories.size();
	}

	public void executeScript()
	{
		try
		{
			ScriptEngine.getInstance().executeScript(ScriptEngine.EFFECT_MASTER_HANDLER_FILE);
		}
		catch (Exception var2)
		{
			throw new Error("Problems while running EffectMasterHandler", var2);
		}
	}

	public static EffectHandler getInstance()
	{
		return EffectHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EffectHandler INSTANCE = new EffectHandler();
	}
}
