package net.sf.l2jdev.gameserver.model.stats.functions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class FuncDiv extends AbstractFunction
{
	public FuncDiv(Stat stat, int order, Object owner, double value, Condition applyCond)
	{
		super(stat, order, owner, value, applyCond);
	}

	@Override
	public double calc(Creature effector, Creature effected, Skill skill, double initVal)
	{
		if (this.getApplyCond() == null || this.getApplyCond().test(effector, effected, skill))
		{
			try
			{
				return initVal / this.getValue();
			}
			catch (Exception var7)
			{
				LOG.warning(FuncDiv.class.getSimpleName() + ": Division by zero: " + this.getValue() + "!");
			}
		}

		return initVal;
	}
}
