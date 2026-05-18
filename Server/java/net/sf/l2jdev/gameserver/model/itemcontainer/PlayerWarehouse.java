package net.sf.l2jdev.gameserver.model.itemcontainer;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;

public class PlayerWarehouse extends Warehouse
{
	private final Player _owner;

	public PlayerWarehouse(Player owner)
	{
		this._owner = owner;
	}

	@Override
	public String getName()
	{
		return "Warehouse";
	}

	@Override
	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.WAREHOUSE;
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return this._items.size() + slots <= this._owner.getWareHouseLimit();
	}
}
