package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.PetLevelData;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.type.SwampZone;

public class SpeedFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double staticSpeed = creature.getStat().getValue(Stat.STATIC_SPEED, 0.0);
		if (staticSpeed > 0.0)
		{
			return staticSpeed;
		}
		double baseValue = this.getBaseSpeed(creature, stat);
		if (creature.isPlayer())
		{
			baseValue += this.calcEnchantBodyPart(creature, BodyPart.FEET);
		}

		byte speedStat = (byte) creature.getStat().getAdd(Stat.STAT_BONUS_SPEED, -1.0);
		if (speedStat >= 0 && speedStat < BaseStat.values().length)
		{
			BaseStat baseStat = BaseStat.values()[speedStat];
			double bonusDex = Math.max(0, baseStat.calcValue(creature) - 55);
			baseValue += bonusDex;
		}

		double maxSpeed;
		if (creature.isPlayer())
		{
			maxSpeed = creature.isGM() ? 10000.0 : PlayerConfig.MAX_RUN_SPEED + creature.getStat().getValue(Stat.SPEED_LIMIT, 0.0);
		}
		else if (creature.isSummon())
		{
			maxSpeed = PlayerConfig.MAX_RUN_SPEED_SUMMON + creature.getStat().getValue(Stat.SPEED_LIMIT, 0.0);
		}
		else
		{
			maxSpeed = Double.MAX_VALUE;
		}

		return this.validateValue(creature, Stat.defaultValue(creature, stat, baseValue), 1.0, maxSpeed);
	}

	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return isBlessed ? Math.max(enchantLevel - 3, 0) + Math.max(enchantLevel - 6, 0) : 0.6 * Math.max(enchantLevel - 3, 0) + 0.6 * Math.max(enchantLevel - 6, 0);
	}

	private double getBaseSpeed(Creature creature, Stat stat)
	{
		double baseValue = this.calcWeaponPlusBaseValue(creature, stat);
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (player.isMounted())
			{
				PetLevelData data = PetDataTable.getInstance().getPetLevelData(player.getMountNpcId(), player.getMountLevel());
				if (data != null)
				{
					baseValue = data.getSpeedOnRide(stat);
					if (player.getMountLevel() - creature.getLevel() >= 10)
					{
						baseValue /= 2.0;
					}

					if (player.isHungry())
					{
						baseValue /= 2.0;
					}
				}
			}

			baseValue += PlayerConfig.RUN_SPD_BOOST;
		}

		if (creature.isPlayable() && creature.isInsideZone(ZoneId.SWAMP))
		{
			SwampZone zone = ZoneManager.getInstance().getZone(creature, SwampZone.class);
			if (zone != null)
			{
				baseValue *= zone.getMoveBonus();
			}
		}

		return baseValue;
	}
}
