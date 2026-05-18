package net.sf.l2jdev.gameserver.data.enums;

import net.sf.l2jdev.gameserver.model.item.Armor;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;

public enum CrystallizationType
{
	NONE,
	WEAPON,
	ARMOR,
	ACCESORY;

	public static CrystallizationType getByItem(ItemTemplate item)
	{
		if (item instanceof Weapon)
		{
			return WEAPON;
		}
		else if (item instanceof Armor)
		{
			return ARMOR;
		}
		else
		{
			switch (item.getBodyPart())
			{
				case R_EAR:
				case L_EAR:
				case LR_EAR:
				case R_FINGER:
				case L_FINGER:
				case LR_FINGER:
				case NECK:
				case HAIR:
				case HAIR2:
				case HAIRALL:
				case ARTIFACT_BOOK:
				case ARTIFACT:
					return ACCESORY;
				default:
					return NONE;
			}
		}
	}
}
