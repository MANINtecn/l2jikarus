package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class VariationRequest extends AbstractRequest
{
	private Item _augmented;
	private Item _mineral;
	private VariationInstance _augmentation;

	public VariationRequest(Player player)
	{
		super(player);
	}

	public synchronized void setAugmentedItem(int objectId)
	{
		this._augmented = this.getPlayer().getInventory().getItemByObjectId(objectId);
	}

	public synchronized Item getAugmentedItem()
	{
		return this._augmented;
	}

	public synchronized void setMineralItem(int objectId)
	{
		this._mineral = this.getPlayer().getInventory().getItemByObjectId(objectId);
	}

	public synchronized Item getMineralItem()
	{
		return this._mineral;
	}

	public synchronized void setAugment(VariationInstance augment)
	{
		this._augmentation = augment;
	}

	public synchronized VariationInstance getAugment()
	{
		return this._augmentation;
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}
}
