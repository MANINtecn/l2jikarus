package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class InitialEquipment extends ItemHolder
{
	private final boolean _equipped;

	public InitialEquipment(StatSet set)
	{
		super(set.getInt("id"), set.getInt("count"));
		this._equipped = set.getBoolean("equipped", false);
	}

	public boolean isEquipped()
	{
		return this._equipped;
	}
}
