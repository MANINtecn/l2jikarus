package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MCritRateFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = this.calcWeaponPlusBaseValue(creature, stat);
		if (creature.isPlayer())
		{
			baseValue += this.calcEnchantBodyPart(creature, BodyPart.LEGS);
		}

		double physicalBonus = (creature.getStat().getMul(Stat.MAGIC_CRITICAL_RATE_BY_CRITICAL_RATE, 1.0) - 1.0) * creature.getStat().getCriticalHit();
		double witBonus = creature.getWIT() > 0 ? BaseStat.WIT.calcBonus(creature) : 1.0;
		double maxMagicalCritRate;
		if (creature.isPlayable())
		{
			maxMagicalCritRate = PlayerConfig.MAX_MCRIT_RATE + creature.getStat().getValue(Stat.ADD_MAX_MAGIC_CRITICAL_RATE, 0.0);
		}
		else
		{
			maxMagicalCritRate = Double.MAX_VALUE;
		}

		return this.validateValue(creature, Stat.defaultValue(creature, stat, baseValue * witBonus * 10.0 + physicalBonus), 0.0, maxMagicalCritRate);
	}

	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return isBlessed ? 0.5 * Math.max(enchantLevel - 3, 0) + 0.5 * Math.max(enchantLevel - 6, 0) : 0.34 * Math.max(enchantLevel - 3, 0) + 0.34 * Math.max(enchantLevel - 6, 0);
	}
}
