package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class MaxMpFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = creature.getTemplate().getBaseValue(stat, 0.0);
		if (creature.isPet())
		{
			Pet pet = creature.asPet();
			baseValue = pet.getPetLevelData().getPetMaxMP();
		}
		else if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (player != null)
			{
				baseValue = player.getTemplate().getBaseMpMax(player.getLevel());
			}
		}

		double menBonus = creature.getMEN() > 0 ? BaseStat.MEN.calcBonus(creature) : 1.0;
		baseValue *= menBonus;
		return defaultValue(creature, stat, baseValue);
	}

	private static double defaultValue(Creature creature, Stat stat, double baseValue)
	{
		double mul = creature.getStat().getMul(stat);
		double add = creature.getStat().getAdd(stat);
		double addItem = 0.0;
		Inventory inv = creature.getInventory();
		if (inv != null)
		{
			for (Item item : inv.getPaperdollItems())
			{
				addItem += item.getTemplate().getStats(stat, 0.0);
			}
		}

		return mul * baseValue + add + addItem + creature.getStat().getMoveTypeValue(stat, creature.getMoveType());
	}
}
