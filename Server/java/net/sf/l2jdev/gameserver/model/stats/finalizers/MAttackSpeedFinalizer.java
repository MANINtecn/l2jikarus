package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.ChampionMonstersConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MAttackSpeedFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double staticMAtkSpeed = creature.getStat().getValue(Stat.STATIC_MAGICAL_ATTACK_SPEED, 0.0);
		if (staticMAtkSpeed > 0.0)
		{
			return staticMAtkSpeed;
		}
		double baseValue = this.calcWeaponBaseValue(creature, stat);
		if (ChampionMonstersConfig.CHAMPION_ENABLE && creature.isChampion())
		{
			baseValue *= ChampionMonstersConfig.CHAMPION_SPD_ATK;
		}

		double witBonus = creature.getWIT() > 0 ? BaseStat.WIT.calcBonus(creature) : 1.0;
		baseValue *= witBonus;
		return this.validateValue(creature, this.defaultValue(creature, stat, baseValue), 1.0, creature.isPlayable() ? PlayerConfig.MAX_MATK_SPEED : Double.MAX_VALUE);
	}

	protected double defaultValue(Creature creature, Stat stat, double baseValue)
	{
		double mul = Math.max(creature.getStat().getMul(stat), 0.7);
		double add = creature.getStat().getAdd(stat);
		return baseValue * mul + add + creature.getStat().getMoveTypeValue(stat, creature.getMoveType());
	}
}
