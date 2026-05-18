package net.sf.l2jdev.gameserver.model.itemcontainer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class PetInventory extends Inventory
{
	private final Pet _owner;

	public PetInventory(Pet owner)
	{
		this._owner = owner;
	}

	@Override
	public Pet getOwner()
	{
		return this._owner;
	}

	@Override
	public int getOwnerId()
	{
		return this.getOwner() == null ? 0 : this._owner.getControlObjectId();
	}

	@Override
	protected void refreshWeight()
	{
		super.refreshWeight();
		this._owner.updateAndBroadcastStatus(1);
	}

	@Override
	public Collection<Item> getItems()
	{
		List<Item> equippedItems = new LinkedList<>();

		for (Item item : super.getItems())
		{
			if (item.isEquipped())
			{
				equippedItems.add(item);
			}
		}

		return equippedItems;
	}

	public boolean validateCapacity(Item item)
	{
		int slots = 0;
		if ((!item.isStackable() || this.getItemByItemId(item.getId()) == null) && !item.getTemplate().hasExImmediateEffect())
		{
			slots++;
		}

		return this.validateCapacity(slots);
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return this._items.size() + slots <= this._owner.getInventoryLimit();
	}

	public boolean validateWeight(Item item, long count)
	{
		int weight = 0;
		ItemTemplate template = ItemData.getInstance().getTemplate(item.getId());
		if (template == null)
		{
			return false;
		}
		weight = (int) (weight + count * template.getWeight());
		return this.validateWeight(weight);
	}

	@Override
	public boolean validateWeight(long weight)
	{
		return this._totalWeight + weight <= this._owner.getMaxLoad();
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}

	public void transferItemsToOwner()
	{
		for (Item item : this._items)
		{
			this.getOwner().transferItem(ItemProcessType.TRANSFER, item.getObjectId(), item.getCount(), this.getOwner().getOwner().getInventory(), this.getOwner().getOwner(), this.getOwner());
		}
	}
}
