package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class CompoundRequest extends AbstractRequest
{
	private int _itemOne;
	private int _itemTwo;

	public CompoundRequest(Player player)
	{
		super(player);
	}

	public Item getItemOne()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._itemOne);
	}

	public void setItemOne(int itemOne)
	{
		this._itemOne = itemOne;
	}

	public Item getItemTwo()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._itemTwo);
	}

	public void setItemTwo(int itemTwo)
	{
		this._itemTwo = itemTwo;
	}

	@Override
	public boolean isItemRequest()
	{
		return true;
	}

	@Override
	public boolean canWorkWith(AbstractRequest request)
	{
		return !request.isItemRequest();
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return objectId > 0 && (objectId == this._itemOne || objectId == this._itemTwo);
	}
}
