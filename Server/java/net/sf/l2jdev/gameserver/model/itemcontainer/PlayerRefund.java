package net.sf.l2jdev.gameserver.model.itemcontainer;

import java.util.logging.Level;

import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class PlayerRefund extends ItemContainer
{
	private final Player _owner;

	public PlayerRefund(Player owner)
	{
		this._owner = owner;
	}

	@Override
	public String getName()
	{
		return "Refund";
	}

	@Override
	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.REFUND;
	}

	@Override
	protected void addItem(Item item)
	{
		super.addItem(item);

		try
		{
			if (this.getSize() > 12)
			{
				Item removedItem = this._items.stream().findFirst().get();
				if (this._items.remove(removedItem))
				{
					ItemManager.destroyItem(ItemProcessType.REFUND, removedItem, this.getOwner(), null);
					removedItem.updateDatabase(true);
				}
			}
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.SEVERE, "addItem()", var3);
		}
	}

	@Override
	public void refreshWeight()
	{
	}

	@Override
	public void deleteMe()
	{
		try
		{
			for (Item item : this._items)
			{
				ItemManager.destroyItem(ItemProcessType.REFUND, item, this.getOwner(), null);
				item.updateDatabase(true);
			}
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.SEVERE, "deleteMe()", var3);
		}

		this._items.clear();
	}

	@Override
	public void restore()
	{
	}
}
