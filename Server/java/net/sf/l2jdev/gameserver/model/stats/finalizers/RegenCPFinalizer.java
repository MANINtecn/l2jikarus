package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class RegenCPFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		if (!creature.isPlayer())
		{
			return 0.0;
		}
		Player player = creature.asPlayer();
		double baseValue = player.getTemplate().getBaseCpRegen(creature.getLevel()) * creature.getLevelMod() * BaseStat.CON.calcBonus(creature) * PlayerConfig.CP_REGEN_MULTIPLIER;
		if (player.isSitting())
		{
			baseValue *= 1.5;
		}
		else if (!player.isMoving())
		{
			baseValue *= 1.1;
		}
		else if (player.isRunning())
		{
			baseValue *= 0.7;
		}

		return Stat.defaultValue(player, stat, baseValue);
	}
}
