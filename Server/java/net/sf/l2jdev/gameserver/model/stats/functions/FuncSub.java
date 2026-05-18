package net.sf.l2jdev.gameserver.model.stats.functions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class FuncSub extends AbstractFunction
{
	public FuncSub(Stat stat, int order, Object owner, double value, Condition applyCond)
	{
		super(stat, order, owner, value, applyCond);
	}

	@Override
	public double calc(Creature effector, Creature effected, Skill skill, double initVal)
	{
		return this.getApplyCond() != null && !this.getApplyCond().test(effector, effected, skill) ? initVal : initVal - this.getValue();
	}
}
