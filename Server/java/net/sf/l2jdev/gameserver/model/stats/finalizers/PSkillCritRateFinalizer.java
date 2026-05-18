package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class PSkillCritRateFinalizer implements IStatFunction
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

		double strBonus = creature.getSTR() > 0 ? BaseStat.STR.calcBonus(creature) : 1.0;
		return this.validateValue(creature, Stat.defaultValue(creature, stat, baseValue * strBonus), 0.0, creature.isPlayable() ? PlayerConfig.MAX_PSKILLCRIT_RATE : Double.MAX_VALUE);
	}

	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return isBlessed ? 0.5 * Math.max(enchantLevel - 3, 0) + 0.5 * Math.max(enchantLevel - 6, 0) : 0.34 * Math.max(enchantLevel - 3, 0) + 0.34 * Math.max(enchantLevel - 6, 0);
	}
}
