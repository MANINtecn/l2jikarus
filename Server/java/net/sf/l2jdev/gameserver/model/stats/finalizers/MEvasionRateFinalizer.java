package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MEvasionRateFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = this.calcWeaponPlusBaseValue(creature, stat);
		if (creature.isPlayer())
		{
			baseValue += this.calcEnchantBodyPart(creature, BodyPart.HEAD);
		}

		return this.validateValue(creature, Stat.defaultValue(creature, stat, baseValue), Double.NEGATIVE_INFINITY, creature.isPlayable() ? PlayerConfig.MAX_EVASION : Double.MAX_VALUE);
	}

	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return isBlessed ? 0.3 * Math.max(enchantLevel - 3, 0) + 0.3 * Math.max(enchantLevel - 6, 0) : 0.2 * Math.max(enchantLevel - 3, 0) + 0.2 * Math.max(enchantLevel - 6, 0);
	}
}
