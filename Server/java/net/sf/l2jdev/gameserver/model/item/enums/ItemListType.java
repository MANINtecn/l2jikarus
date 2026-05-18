package net.sf.l2jdev.gameserver.model.item.enums;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public enum ItemListType implements IUpdateTypeComponent
{
	AUGMENT_BONUS(1),
	ELEMENTAL_ATTRIBUTE(2),
	VISUAL_ID(4),
	SOUL_CRYSTAL(8),
	REUSE_DELAY(16),
	PET_EVOLVE(64),
	BLESSED(128);

	private final int _mask;

	private ItemListType(int mask)
	{
		this._mask = mask;
	}

	@Override
	public int getMask()
	{
		return this._mask;
	}
}
