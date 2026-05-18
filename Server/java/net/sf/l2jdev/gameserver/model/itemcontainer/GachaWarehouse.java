package net.sf.l2jdev.gameserver.model.itemcontainer;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;

public class GachaWarehouse extends Warehouse
{
	public static final int MAXIMUM_TEMPORARY_WAREHOUSE_COUNT = 1100;
	private final Creature _owner;

	public GachaWarehouse(Player player)
	{
		this._owner = player;
	}

	@Override
	protected Creature getOwner()
	{
		return this._owner;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.GACHA;
	}
}
