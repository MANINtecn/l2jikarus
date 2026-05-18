package net.sf.l2jdev.gameserver.data.holders;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.model.siege.TaxType;

public class PreparedMultisellListHolder extends MultisellListHolder
{
	private int _npcObjectId;
	private final boolean _inventoryOnly;
	private double _taxRate;
	private List<ItemInfo> _itemInfos;

	public PreparedMultisellListHolder(MultisellListHolder list, boolean inventoryOnly, ItemContainer inventory, Npc npc, double ingredientMultiplier, double productMultiplier)
	{
		super(list.getId(), list.isChanceMultisell(), list.isApplyTaxes(), list.isMaintainEnchantment(), list.getIngredientMultiplier(), list.getProductMultiplier(), list._entries, list._npcsAllowed);
		this._inventoryOnly = inventoryOnly;
		if (npc != null)
		{
			this._npcObjectId = npc.getObjectId();
			this._taxRate = npc.getCastleTaxRate(TaxType.BUY);
		}

		if (inventoryOnly)
		{
			this._entries = new ArrayList<>();
			this._itemInfos = new ArrayList<>();

			for (Item item : inventory.getItems())
			{
				if (!item.isEquipped() && (item.isArmor() || item.isWeapon()))
				{
					for (MultisellEntryHolder entry : list.getEntries())
					{
						for (ItemChanceHolder holder : entry.getIngredients())
						{
							if (holder.getId() == item.getId())
							{
								this._entries.add(entry);
								this._itemInfos.add(new ItemInfo(item));
							}
						}
					}
				}
			}
		}
	}

	public ItemInfo getItemEnchantment(int index)
	{
		return this._itemInfos != null ? this._itemInfos.get(index) : null;
	}

	public double getTaxRate()
	{
		return this.isApplyTaxes() ? this._taxRate : 0.0;
	}

	public boolean isInventoryOnly()
	{
		return this._inventoryOnly;
	}

	public boolean checkNpcObjectId(int npcObjectId)
	{
		return this._npcObjectId == 0 || this._npcObjectId == npcObjectId;
	}

	public long getIngredientCount(ItemHolder ingredient)
	{
		return ingredient.getId() == 57 ? Math.round(ingredient.getCount() * this.getIngredientMultiplier() * (1.0 + this.getTaxRate())) : Math.round(ingredient.getCount() * this.getIngredientMultiplier());
	}

	public long getProductCount(ItemChanceHolder product)
	{
		return Math.round(product.getCount() * this.getProductMultiplier());
	}
}
