package net.sf.l2jdev.gameserver.model.stats;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;

@FunctionalInterface
public interface IStatFunction
{
	default void throwIfPresent(OptionalDouble base)
	{
		if (base.isPresent())
		{
			throw new IllegalArgumentException("base should not be set for " + this.getClass().getSimpleName());
		}
	}

	default double calcEnchantBodyPart(Creature creature, BodyPart... bodyParts)
	{
		double value = 0.0;

		for (BodyPart bodyPart : bodyParts)
		{
			Item item = creature.getInventory().getPaperdollItemByBodyPart(bodyPart);
			if (item != null && item.getEnchantLevel() >= 4 && item.getTemplate().getCrystalTypePlus() == CrystalType.R)
			{
				value += this.calcEnchantBodyPartBonus(item.getEnchantLevel(), item.getTemplate().isBlessed());
			}
		}

		return value;
	}

	default double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return 0.0;
	}

	default double calcWeaponBaseValue(Creature creature, Stat stat)
	{
		double baseTemplateValue = creature.getTemplate().getBaseValue(stat, 0.0);
		Transform transform = creature.getTransformation();
		double baseValue = transform == null ? baseTemplateValue : transform.getStats(creature, stat, baseTemplateValue);
		if (creature.isPet())
		{
			Pet pet = creature.asPet();
			Item weapon = pet.getActiveWeaponInstance();
			double baseVal = stat == Stat.PHYSICAL_ATTACK ? pet.getPetLevelData().getPetPAtk() : (stat == Stat.MAGIC_ATTACK ? pet.getPetLevelData().getPetMAtk() : baseTemplateValue);
			baseValue = baseVal + (weapon != null ? weapon.getTemplate().getStats(stat, baseVal) : 0.0);
		}
		else if (creature.isPlayer() && (transform == null || transform.canUseWeaponStats()))
		{
			Item weapon = creature.getActiveWeaponInstance();
			baseValue = weapon != null ? weapon.getTemplate().getStats(stat, baseTemplateValue) : baseTemplateValue;
		}

		return baseValue;
	}

	default double calcWeaponPlusBaseValue(Creature creature, Stat stat)
	{
		double baseTemplateValue = creature.getTemplate().getBaseValue(stat, 0.0);
		Transform transform = creature.getTransformation();
		double baseValue = transform != null && !transform.isStance() ? transform.getStats(creature, stat, baseTemplateValue) : baseTemplateValue;
		if (creature.isPlayable())
		{
			Inventory inv = creature.getInventory();
			if (inv != null)
			{
				baseValue += inv.getPaperdollCache().getStats(stat);
			}
		}

		return baseValue;
	}

	default double calcEnchantedItemBonus(Creature creature, Stat stat)
	{
		if (!creature.isPlayer())
		{
			return 0.0;
		}
		double value = 0.0;

		for (Item equippedItem : creature.getInventory().getPaperdollItems(Item::isEnchanted))
		{
			ItemTemplate item = equippedItem.getTemplate();
			BodyPart bodyPart = item.getBodyPart();
			if (bodyPart != BodyPart.HAIR && bodyPart != BodyPart.HAIR2 && bodyPart != BodyPart.HAIRALL ? !(item.getStats(stat, 0.0) <= 0.0) : stat == Stat.PHYSICAL_DEFENCE || stat == Stat.MAGICAL_DEFENCE)
			{
				double blessedBonus = item.isBlessed() ? 1.5 : 1.0;
				int enchant = equippedItem.getEnchantLevel();
				if (creature.asPlayer().isInOlympiadMode())
				{
					if (item.isWeapon())
					{
						if (OlympiadConfig.OLYMPIAD_WEAPON_ENCHANT_LIMIT >= 0 && enchant > OlympiadConfig.OLYMPIAD_WEAPON_ENCHANT_LIMIT)
						{
							enchant = OlympiadConfig.OLYMPIAD_WEAPON_ENCHANT_LIMIT;
						}
					}
					else if (OlympiadConfig.OLYMPIAD_ARMOR_ENCHANT_LIMIT >= 0 && enchant > OlympiadConfig.OLYMPIAD_ARMOR_ENCHANT_LIMIT)
					{
						enchant = OlympiadConfig.OLYMPIAD_ARMOR_ENCHANT_LIMIT;
					}
				}

				if (stat == Stat.MAGICAL_DEFENCE)
				{
					value += calcEnchantmDefBonus(equippedItem, blessedBonus, enchant);
				}
				else if (stat == Stat.PHYSICAL_DEFENCE)
				{
					value += calcEnchantDefBonus(equippedItem, blessedBonus, enchant);
				}
				else if (stat == Stat.MAGIC_ATTACK)
				{
					value += calcEnchantMatkBonus(equippedItem, blessedBonus, enchant);
				}
				else if (stat == Stat.PHYSICAL_ATTACK && equippedItem.isWeapon())
				{
					value += calcEnchantedPAtkBonus(equippedItem, blessedBonus, enchant);
				}
			}
		}

		return value;
	}

	static double calcEnchantmDefBonus(Item item, double blessedBonus, int enchant)
	{
		switch (item.getTemplate().getCrystalTypePlus())
		{
			case S:
				return 5 * enchant + 10 * Math.max(0, enchant - 3);
			case A:
				return 3 * enchant + 4 * Math.max(0, enchant - 3);
			default:
				return enchant + 3 * Math.max(0, enchant - 3);
		}
	}

	static double calcEnchantDefBonus(Item item, double blessedBonus, int enchant)
	{
		switch (item.getTemplate().getCrystalTypePlus())
		{
			case S:
				return 7 * enchant + 14 * Math.max(0, enchant - 3);
			case A:
				return 4 * enchant + 5 * Math.max(0, enchant - 3);
			default:
				return enchant + 3 * Math.max(0, enchant - 3);
		}
	}

	static double calcEnchantMatkBonus(Item item, double blessedBonus, int enchant)
	{
		switch (item.getTemplate().getCrystalTypePlus())
		{
			case S:
				return 10 * enchant + 20 * Math.max(0, enchant - 3);
			case A:
				if (item.getWeaponItem().isImmortalityWeapon())
				{
					if (item.getItemType() == WeaponType.BLUNT)
					{
						return 55 * enchant;
					}

					return 50 * enchant;
				}

				return 6 * enchant + 12 * Math.max(0, enchant - 3) + getFrostLordWeaponBonus(item, enchant) * 18 * Math.max(0, enchant - 7);
			case B:
			case C:
			case D:
				return 3 * enchant + 3 * Math.max(0, enchant - 3);
			default:
				return 2 * enchant + 2 * Math.max(0, enchant - 3);
		}
	}

	static double calcEnchantedPAtkBonus(Item item, double blessedBonus, int enchant)
	{
		switch (item.getTemplate().getCrystalTypePlus())
		{
			case S:
				if (item.getWeaponItem().getBodyPart() == BodyPart.LR_HAND && item.getWeaponItem().getItemType() != WeaponType.POLE)
				{
					if (item.getWeaponItem().getItemType().isRanged())
					{
						return 31 * enchant + 62 * Math.max(0, enchant - 3);
					}

					return 19 * enchant + 38 * Math.max(0, enchant - 3);
				}

				return 15 * enchant + 30 * Math.max(0, enchant - 3);
			case A:
				Weapon weapon = item.getWeaponItem();
				if (weapon.getBodyPart() == BodyPart.LR_HAND && weapon.getItemType() != WeaponType.POLE)
				{
					if (weapon.getItemType().isRanged())
					{
						if (weapon.isImmortalityWeapon())
						{
							return 128 * enchant;
						}

						return 16 * enchant + 32 * Math.max(0, enchant - 3) + getFrostLordWeaponBonus(item, enchant) * 48 * Math.max(0, enchant - 7);
					}
					if (weapon.isImmortalityWeapon())
					{
						return 96 * enchant;
					}

					return 12 * enchant + 24 * Math.max(0, enchant - 3) + getFrostLordWeaponBonus(item, enchant) * 36 * Math.max(0, enchant - 7);
				}
				if (weapon.isImmortalityWeapon())
				{
					return 80 * enchant;
				}

				return 10 * enchant + 20 * Math.max(0, enchant - 3) + getFrostLordWeaponBonus(item, enchant) * 30 * Math.max(0, enchant - 7);
			case B:
			case C:
			case D:
				Weapon weaponBcd = item.getWeaponItem();
				if (weaponBcd.getBodyPart() == BodyPart.LR_HAND && weaponBcd.getItemType() != WeaponType.POLE)
				{
					if (weaponBcd.getItemType().isRanged())
					{
						return 8 * enchant + 8 * Math.max(0, enchant - 3);
					}

					return 5 * enchant + 5 * Math.max(0, enchant - 3);
				}

				return 4 * enchant + 4 * Math.max(0, enchant - 3);
			default:
				return item.getWeaponItem().getItemType().isRanged() ? 4 * enchant + 4 * Math.max(0, enchant - 3) : 2 * enchant + 2 * Math.max(0, enchant - 3);
		}
	}

	static int getFrostLordWeaponBonus(Item item, int enchant)
	{
		return Boolean.compare(enchant >= 8 && (95725 <= item.getId() && item.getId() <= 95737 || 96751 <= item.getId() && item.getId() <= 96763), false);
	}

	default double validateValue(Creature creature, double value, double minValue, double maxValue)
	{
		return value > maxValue ? maxValue : Math.max(minValue, value);
	}

	double calc(Creature var1, OptionalDouble var2, Stat var3);
}
