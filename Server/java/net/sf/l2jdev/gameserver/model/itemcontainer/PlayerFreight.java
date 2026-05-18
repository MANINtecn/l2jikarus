package net.sf.l2jdev.gameserver.model.itemcontainer;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;

public class PlayerFreight extends ItemContainer
{
	private final Player _owner;
	private final int _ownerId;

	public PlayerFreight(int objectId)
	{
		this._owner = null;
		this._ownerId = objectId;
		this.restore();
	}

	public PlayerFreight(Player owner)
	{
		this._owner = owner;
		this._ownerId = owner.getObjectId();
	}

	@Override
	public int getOwnerId()
	{
		return this._ownerId;
	}

	@Override
	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}

	@Override
	public String getName()
	{
		return "Freight";
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return this.getSize() + slots <= PlayerConfig.ALT_FREIGHT_SLOTS;
	}

	@Override
	public void refreshWeight()
	{
	}
}
