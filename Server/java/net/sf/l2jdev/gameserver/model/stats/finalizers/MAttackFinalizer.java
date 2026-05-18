package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.ChampionMonstersConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MAttackFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = this.calcWeaponBaseValue(creature, stat);
		if (creature.getActiveWeaponInstance() != null)
		{
			baseValue += creature.getStat().getWeaponBonusMAtk();
			baseValue *= creature.getStat().getMul(Stat.WEAPON_BONUS_MAGIC_ATTACK_MULTIPIER, 1.0);
		}

		baseValue += this.calcEnchantedItemBonus(creature, stat);
		if (creature.isPlayer())
		{
			baseValue += this.calcEnchantBodyPart(creature, BodyPart.CHEST, BodyPart.FULL_ARMOR);
		}

		if (ChampionMonstersConfig.CHAMPION_ENABLE && creature.isChampion())
		{
			baseValue *= ChampionMonstersConfig.CHAMPION_ATK;
		}

		if (creature.isRaid())
		{
			baseValue *= NpcConfig.RAID_MATTACK_MULTIPLIER;
		}

		double physicalBonus = (creature.getStat().getMul(Stat.MAGIC_ATTACK_BY_PHYSICAL_ATTACK, 1.0) - 1.0) * creature.getPAtk();
		baseValue *= Math.pow(BaseStat.INT.calcBonus(creature) * creature.getLevelMod(), 2.2072);
		return this.validateValue(creature, Stat.defaultValue(creature, stat, baseValue + physicalBonus), 0.0, creature.isPlayable() ? PlayerConfig.MAX_MATK : Double.MAX_VALUE);
	}

	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return isBlessed ? 2 * Math.max(enchantLevel - 3, 0) + 2 * Math.max(enchantLevel - 6, 0) : 1.4 * Math.max(enchantLevel - 3, 0) + 1.4 * Math.max(enchantLevel - 6, 0);
	}
}
