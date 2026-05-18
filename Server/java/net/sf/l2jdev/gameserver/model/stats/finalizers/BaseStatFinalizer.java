package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class BaseStatFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = creature.getTemplate().getBaseValue(stat, 0.0);
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			baseValue += player.getInventory().getPaperdollCache().getBaseStatValue(player, BaseStat.valueOf(stat));
			baseValue += player.getHennaValue(BaseStat.valueOf(stat));
			switch (stat)
			{
				case STAT_STR:
					baseValue += player.getVariables().getInt("STAT_STR", 0);
					break;
				case STAT_CON:
					baseValue += player.getVariables().getInt("STAT_CON", 0);
					break;
				case STAT_DEX:
					baseValue += player.getVariables().getInt("STAT_DEX", 0);
					break;
				case STAT_INT:
					baseValue += player.getVariables().getInt("STAT_INT", 0);
					break;
				case STAT_MEN:
					baseValue += player.getVariables().getInt("STAT_MEN", 0);
					break;
				case STAT_WIT:
					baseValue += player.getVariables().getInt("STAT_WIT", 0);
			}
		}

		return this.validateValue(creature, Stat.defaultValue(creature, stat, baseValue), 1.0, 200.0);
	}
}
