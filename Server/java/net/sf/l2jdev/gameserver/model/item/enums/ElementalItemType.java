package net.sf.l2jdev.gameserver.model.item.enums;

public enum ElementalItemType
{
	STONE(3),
	STONE_SUPER(3),
	CRYSTAL(6),
	CRYSTAL_SUPER(6),
	JEWEL(9),
	ENERGY(12);

	private final int _maxLevel;

	private ElementalItemType(int maxLevel)
	{
		this._maxLevel = maxLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}
}
