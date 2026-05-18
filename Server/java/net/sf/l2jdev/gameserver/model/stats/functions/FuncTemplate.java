package net.sf.l2jdev.gameserver.model.stats.functions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class FuncTemplate
{
	private final Class<?> _functionClass;
	private final Condition _attachCond;
	private final Condition _applyCond;
	private final Stat _stat;
	private final int _order;
	private final double _value;

	public FuncTemplate(Condition attachCond, Condition applyCond, String functionName, int order, Stat stat, double value)
	{
		StatFunction function = StatFunction.valueOf(functionName.toUpperCase());
		if (order >= 0)
		{
			this._order = order;
		}
		else
		{
			this._order = function.getOrder();
		}

		this._attachCond = attachCond;
		this._applyCond = applyCond;
		this._stat = stat;
		this._value = value;

		try
		{
			this._functionClass = Class.forName("net.sf.l2jdev.gameserver.model.stats.functions.Func" + function.getName());
		}
		catch (ClassNotFoundException var10)
		{
			throw new RuntimeException(var10);
		}
	}

	public Class<?> getFunctionClass()
	{
		return this._functionClass;
	}

	public Stat getStat()
	{
		return this._stat;
	}

	public int getOrder()
	{
		return this._order;
	}

	public double getValue()
	{
		return this._value;
	}

	public boolean meetCondition(Creature effected, Skill skill)
	{
		return this._attachCond != null && !this._attachCond.test(effected, effected, skill) ? false : this._applyCond == null || this._applyCond.test(effected, effected, skill);
	}
}
