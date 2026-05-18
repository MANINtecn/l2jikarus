package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.conditions.ICondition;
import net.sf.l2jdev.gameserver.scripting.ScriptEngine;

public class ConditionHandler
{
	private final Map<String, Function<StatSet, ICondition>> _conditionHandlerFactories = new HashMap<>();

	public void registerHandler(String name, Function<StatSet, ICondition> handlerFactory)
	{
		this._conditionHandlerFactories.put(name, handlerFactory);
	}

	public Function<StatSet, ICondition> getHandlerFactory(String name)
	{
		return this._conditionHandlerFactories.get(name);
	}

	public int size()
	{
		return this._conditionHandlerFactories.size();
	}

	public void executeScript()
	{
		try
		{
			ScriptEngine.getInstance().executeScript(ScriptEngine.CONDITION_HANDLER_FILE);
		}
		catch (Exception var2)
		{
			throw new Error("Problems while running ConditionMasterHandler", var2);
		}
	}

	public static ConditionHandler getInstance()
	{
		return ConditionHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ConditionHandler INSTANCE = new ConditionHandler();
	}
}
