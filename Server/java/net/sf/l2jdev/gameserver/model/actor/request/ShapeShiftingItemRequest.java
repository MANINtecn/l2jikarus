package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class ShapeShiftingItemRequest extends AbstractRequest
{
	private Item _appearanceStone;
	private Item _appearanceExtractItem;

	public ShapeShiftingItemRequest(Player player, Item appearanceStone)
	{
		super(player);
		this._appearanceStone = appearanceStone;
	}

	public Item getAppearanceStone()
	{
		return this._appearanceStone;
	}

	public void setAppearanceStone(Item appearanceStone)
	{
		this._appearanceStone = appearanceStone;
	}

	public Item getAppearanceExtractItem()
	{
		return this._appearanceExtractItem;
	}

	public void setAppearanceExtractItem(Item appearanceExtractItem)
	{
		this._appearanceExtractItem = appearanceExtractItem;
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
		return this._appearanceStone != null && this._appearanceExtractItem != null ? objectId > 0 && (objectId == this._appearanceStone.getObjectId() || objectId == this._appearanceExtractItem.getObjectId()) : false;
	}
}
