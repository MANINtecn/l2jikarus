package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MaxCpFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = creature.getTemplate().getBaseValue(stat, 0.0);
		Player player = creature.asPlayer();
		if (player != null)
		{
			baseValue = player.getTemplate().getBaseCpMax(player.getLevel());
		}

		double conBonus = creature.getCON() > 0 ? BaseStat.CON.calcBonus(creature) : 1.0;
		baseValue *= conBonus;
		return Stat.defaultValue(creature, stat, baseValue);
	}
}
