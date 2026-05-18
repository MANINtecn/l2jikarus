package net.sf.l2jdev.gameserver.model.item.type;

import net.sf.l2jdev.gameserver.model.stats.TraitType;

public enum WeaponType implements ItemType
{
	NONE(TraitType.NONE),
	SWORD(TraitType.SWORD),
	BLUNT(TraitType.BLUNT),
	DAGGER(TraitType.DAGGER),
	POLE(TraitType.POLE),
	DUALFIST(TraitType.DUALFIST),
	BOW(TraitType.BOW),
	ETC(TraitType.ETC),
	DUAL(TraitType.DUAL),
	FIST(TraitType.FIST),
	FISHINGROD(TraitType.NONE),
	RAPIER(TraitType.RAPIER),
	CROSSBOW(TraitType.CROSSBOW),
	ANCIENTSWORD(TraitType.ANCIENTSWORD),
	FLAG(TraitType.NONE),
	DUALDAGGER(TraitType.DUALDAGGER),
	OWNTHING(TraitType.NONE),
	TWOHANDCROSSBOW(TraitType.TWOHANDCROSSBOW),
	DUALBLUNT(TraitType.DUALBLUNT),
	PISTOLS(TraitType.PISTOLS);

	private final int _mask = 1 << this.ordinal();
	private final TraitType _traitType;

	private WeaponType(TraitType traitType)
	{
		this._traitType = traitType;
	}

	@Override
	public int mask()
	{
		return this._mask;
	}

	public TraitType getTraitType()
	{
		return this._traitType;
	}

	public boolean isRanged()
	{
		return this == BOW || this == CROSSBOW || this == TWOHANDCROSSBOW || this == PISTOLS;
	}

	public boolean isCrossbow()
	{
		return this == CROSSBOW || this == TWOHANDCROSSBOW;
	}

	public boolean isPistols()
	{
		return this == PISTOLS;
	}

	public boolean isDual()
	{
		return this == DUALFIST || this == DUAL || this == DUALDAGGER || this == DUALBLUNT;
	}
}
