package net.sf.l2jdev.gameserver.model.item.appearance;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;

public class AppearanceHolder
{
	private final int _visualId;
	private final WeaponType _weaponType;
	private final ArmorType _armorType;
	private final AppearanceHandType _handType;
	private final AppearanceMagicType _magicType;
	private final AppearanceTargetType _targetType;
	private final BodyPart _bodyPart;

	public AppearanceHolder(StatSet set)
	{
		this._visualId = set.getInt("id", 0);
		this._weaponType = set.getEnum("weaponType", WeaponType.class, WeaponType.NONE);
		this._armorType = set.getEnum("armorType", ArmorType.class, ArmorType.NONE);
		this._handType = set.getEnum("handType", AppearanceHandType.class, AppearanceHandType.NONE);
		this._magicType = set.getEnum("magicType", AppearanceMagicType.class, AppearanceMagicType.NONE);
		this._targetType = set.getEnum("targetType", AppearanceTargetType.class, AppearanceTargetType.NONE);
		this._bodyPart = BodyPart.fromName(set.getString("bodyPart", "none"));
	}

	public WeaponType getWeaponType()
	{
		return this._weaponType;
	}

	public ArmorType getArmorType()
	{
		return this._armorType;
	}

	public AppearanceHandType getHandType()
	{
		return this._handType;
	}

	public AppearanceMagicType getMagicType()
	{
		return this._magicType;
	}

	public AppearanceTargetType getTargetType()
	{
		return this._targetType;
	}

	public BodyPart getBodyPart()
	{
		return this._bodyPart;
	}

	public int getVisualId()
	{
		return this._visualId;
	}
}
