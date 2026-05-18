package net.sf.l2jdev.gameserver.model.item.type;

public enum ArmorType implements ItemType
{
	NONE,
	LIGHT,
	HEAVY,
	MAGIC,
	SIGIL,
	SHIELD;

	final int _mask = 1 << this.ordinal() + WeaponType.values().length;

	@Override
	public int mask()
	{
		return this._mask;
	}
}
