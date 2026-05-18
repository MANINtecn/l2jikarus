package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MDefenseFinalizer implements IStatFunction
{
	private static final int[] SLOTS = new int[]
	{
		14,
		13,
		9,
		8,
		4
	};

	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = creature.getTemplate().getBaseValue(stat, 0.0);
		if (creature.isPet())
		{
			Pet pet = creature.asPet();
			baseValue = pet.getPetLevelData().getPetMDef();
		}

		baseValue += this.calcEnchantedItemBonus(creature, stat);
		Inventory inv = creature.getInventory();
		if (inv != null)
		{
			for (Item item : inv.getPaperdollItems())
			{
				baseValue += item.getTemplate().getStats(stat, 0.0);
			}
		}

		if (creature.isPlayer())
		{
			double accessoryMagicalDefence = creature.getStat().getValue(Stat.ACCESSORY_MAGICAL_DEFENCE, 0.0) / 5.0;
			Player player = creature.asPlayer();
			Transform transform = creature.getTransformation();

			for (int slot : SLOTS)
			{
				if (!player.getInventory().isPaperdollSlotEmpty(slot))
				{
					baseValue -= transform == null ? player.getTemplate().getBaseDefBySlot(slot) : transform.getBaseDefBySlot(player, slot);
					if (accessoryMagicalDefence > 0.0)
					{
						baseValue += accessoryMagicalDefence;
					}
				}
			}
		}
		else if (creature.isPet() && creature.getInventory().getPaperdollObjectId(4) != 0)
		{
			baseValue -= 13.0;
		}

		if (creature.isRaid())
		{
			baseValue *= NpcConfig.RAID_MDEFENCE_MULTIPLIER;
		}

		double bonus = creature.getMEN() > 0 ? BaseStat.MEN.calcBonus(creature) : 1.0;
		baseValue *= bonus * creature.getLevelMod();
		return this.defaultValue(creature, stat, baseValue);
	}

	protected double defaultValue(Creature creature, Stat stat, double baseValue)
	{
		double mul = Math.max(creature.getStat().getMul(stat), 0.5);
		double add = creature.getStat().getAdd(stat);
		return Math.max(baseValue * mul + add + creature.getStat().getMoveTypeValue(stat, creature.getMoveType()), creature.getTemplate().getBaseValue(stat, 0.0) * 0.2);
	}
}
