package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.enchant.attribute.AttributeHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class AttributeFinalizer implements IStatFunction
{
	private final AttributeType _type;
	private final boolean _isWeapon;

	public AttributeFinalizer(AttributeType type, boolean isWeapon)
	{
		this._type = type;
		this._isWeapon = isWeapon;
	}

	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = creature.getTemplate().getBaseValue(stat, 0.0);
		if (creature.isPlayable())
		{
			if (this._isWeapon)
			{
				Item weapon = creature.getActiveWeaponInstance();
				if (weapon != null)
				{
					AttributeHolder weaponInstanceHolder = weapon.getAttribute(this._type);
					if (weaponInstanceHolder != null)
					{
						baseValue += weaponInstanceHolder.getValue();
					}

					AttributeHolder weaponHolder = weapon.getTemplate().getAttribute(this._type);
					if (weaponHolder != null)
					{
						baseValue += weaponHolder.getValue();
					}
				}
			}
			else
			{
				Inventory inventory = creature.getInventory();
				if (inventory != null)
				{
					for (Item item : inventory.getPaperdollItems(Item::isArmor))
					{
						AttributeHolder weaponInstanceHolderx = item.getAttribute(this._type);
						if (weaponInstanceHolderx != null)
						{
							baseValue += weaponInstanceHolderx.getValue();
						}

						AttributeHolder weaponHolder = item.getTemplate().getAttribute(this._type);
						if (weaponHolder != null)
						{
							baseValue += weaponHolder.getValue();
						}
					}
				}
			}
		}

		return Stat.defaultValue(creature, stat, baseValue);
	}
}
