package net.sf.l2jdev.gameserver.model.item;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;

public class Armor extends ItemTemplate
{
	private ArmorType _type;

	public Armor(StatSet set)
	{
		super(set);
	}

	@Override
	public void set(StatSet set)
	{
		super.set(set);
		this._type = set.getEnum("armor_type", ArmorType.class, ArmorType.NONE);
		BodyPart bodyPart = this.getBodyPart();
		if (bodyPart == BodyPart.ARTIFACT || bodyPart == BodyPart.AGATHION)
		{
			this._type1 = 1;
			this._type2 = 2;
		}
		else if (bodyPart != BodyPart.NECK && bodyPart != BodyPart.L_EAR && bodyPart != BodyPart.R_EAR && bodyPart != BodyPart.LR_EAR && bodyPart != BodyPart.L_FINGER && bodyPart != BodyPart.R_FINGER && bodyPart != BodyPart.LR_FINGER && bodyPart != BodyPart.R_BRACELET && bodyPart != BodyPart.L_BRACELET && bodyPart != BodyPart.ARTIFACT_BOOK)
		{
			if (this._type == ArmorType.NONE && bodyPart == BodyPart.L_HAND)
			{
				this._type = ArmorType.SHIELD;
			}

			this._type1 = 1;
			this._type2 = 1;
		}
		else
		{
			this._type1 = 0;
			this._type2 = 2;
		}
	}

	@Override
	public ArmorType getItemType()
	{
		return this._type;
	}

	@Override
	public int getItemMask()
	{
		return this._type.mask();
	}

	@Override
	public boolean isArmor()
	{
		return true;
	}
}
