package net.sf.l2jdev.gameserver.data.holders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.item.enums.UpgradeDataType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;

public class EquipmentUpgradeNormalHolder
{
	private final int _id;
	private final int _type;
	private final long _commission;
	private final double _chance;
	private final ItemEnchantHolder _initialItem;
	private final double _chanceToReceiveBonusItems;
	private final Map<UpgradeDataType, List<ItemEnchantHolder>> _items = new HashMap<>();

	public EquipmentUpgradeNormalHolder(int id, int type, long commission, double chance, ItemEnchantHolder initialItem, List<ItemEnchantHolder> materialItems, List<ItemEnchantHolder> onSuccessItems, List<ItemEnchantHolder> onFailureItems, double chanceToReceiveBonusItems, List<ItemEnchantHolder> bonusItems)
	{
		this._id = id;
		this._type = type;
		this._commission = commission;
		this._chance = chance;
		this._initialItem = initialItem;
		this._chanceToReceiveBonusItems = chanceToReceiveBonusItems;
		if (materialItems != null)
		{
			this._items.put(UpgradeDataType.MATERIAL, materialItems);
		}

		this._items.put(UpgradeDataType.ON_SUCCESS, onSuccessItems);
		if (onFailureItems != null)
		{
			this._items.put(UpgradeDataType.ON_FAILURE, onFailureItems);
		}

		if (bonusItems != null)
		{
			this._items.put(UpgradeDataType.BONUS_TYPE, bonusItems);
		}
	}

	public int getId()
	{
		return this._id;
	}

	public int getType()
	{
		return this._type;
	}

	public long getCommission()
	{
		return this._commission;
	}

	public double getChance()
	{
		return this._chance;
	}

	public ItemEnchantHolder getInitialItem()
	{
		return this._initialItem;
	}

	public double getChanceToReceiveBonusItems()
	{
		return this._chanceToReceiveBonusItems;
	}

	public List<ItemEnchantHolder> getItems(UpgradeDataType upgradeDataType)
	{
		return this._items.get(upgradeDataType);
	}

	public boolean isHasCategory(UpgradeDataType upgradeDataType)
	{
		return this._items.isEmpty() ? false : this._items.containsKey(upgradeDataType);
	}
}
