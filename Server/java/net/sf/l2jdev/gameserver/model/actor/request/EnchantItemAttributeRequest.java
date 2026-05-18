package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class EnchantItemAttributeRequest extends AbstractRequest
{
	private volatile int _enchantingItemObjectId;
	private volatile int _enchantingStoneObjectId;

	public EnchantItemAttributeRequest(Player player, int enchantingStoneObjectId)
	{
		super(player);
		this._enchantingStoneObjectId = enchantingStoneObjectId;
	}

	public Item getEnchantingItem()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._enchantingItemObjectId);
	}

	public void setEnchantingItem(int objectId)
	{
		this._enchantingItemObjectId = objectId;
	}

	public Item getEnchantingStone()
	{
		return this.getPlayer().getInventory().getItemByObjectId(this._enchantingStoneObjectId);
	}

	public void setEnchantingStone(int objectId)
	{
		this._enchantingStoneObjectId = objectId;
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
		return objectId > 0 && (objectId == this._enchantingItemObjectId || objectId == this._enchantingStoneObjectId);
	}
}
