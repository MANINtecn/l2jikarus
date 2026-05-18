package net.sf.l2jdev.gameserver.model.primeshop;

import java.util.List;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class PrimeShopGroup
{
	private final int _brId;
	private final int _category;
	private final int _paymentType;
	private final int _price;
	private final int _panelType;
	private final int _recommended;
	private final int _start;
	private final int _end;
	private final int _daysOfWeek;
	private final int _startHour;
	private final int _startMinute;
	private final int _stopHour;
	private final int _stopMinute;
	private final int _stock;
	private final int _maxStock;
	private final int _salePercent;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _minBirthday;
	private final int _maxBirthday;
	private final int _accountDailyLimit;
	private final int _accountBuyLimit;
	private final boolean _isVipGift;
	private final int _vipTier;
	private final List<PrimeShopItem> _items;

	public PrimeShopGroup(StatSet set, List<PrimeShopItem> items)
	{
		this._brId = set.getInt("id");
		this._category = set.getInt("cat", 0);
		this._paymentType = set.getInt("paymentType", 0);
		this._price = set.getInt("price");
		this._panelType = set.getInt("panelType", 0);
		this._recommended = set.getInt("recommended", 0);
		this._start = set.getInt("startSale", 0);
		this._end = set.getInt("endSale", 0);
		this._daysOfWeek = set.getInt("daysOfWeek", 127);
		this._startHour = set.getInt("startHour", 0);
		this._startMinute = set.getInt("startMinute", 0);
		this._stopHour = set.getInt("stopHour", 0);
		this._stopMinute = set.getInt("stopMinute", 0);
		this._stock = set.getInt("stock", 0);
		this._maxStock = set.getInt("maxStock", -1);
		this._salePercent = set.getInt("salePercent", 0);
		this._minLevel = set.getInt("minLevel", 0);
		this._maxLevel = set.getInt("maxLevel", 0);
		this._minBirthday = set.getInt("minBirthday", 0);
		this._maxBirthday = set.getInt("maxBirthday", 0);
		this._accountDailyLimit = set.getInt("accountDailyLimit", 0);
		this._accountBuyLimit = set.getInt("accountBuyLimit", 0);
		this._isVipGift = set.getBoolean("isVipGift", false);
		this._vipTier = set.getInt("vipTier", 0);
		this._items = items;
	}

	public int getBrId()
	{
		return this._brId;
	}

	public int getCat()
	{
		return this._category;
	}

	public int getPaymentType()
	{
		return this._paymentType;
	}

	public int getPrice()
	{
		return this._price;
	}

	public long getCount()
	{
		return this._items.stream().mapToLong(ItemHolder::getCount).sum();
	}

	public int getWeight()
	{
		return this._items.stream().mapToInt(PrimeShopItem::getWeight).sum();
	}

	public int getPanelType()
	{
		return this._panelType;
	}

	public int getRecommended()
	{
		return this._recommended;
	}

	public int getStartSale()
	{
		return this._start;
	}

	public int getEndSale()
	{
		return this._end;
	}

	public int getDaysOfWeek()
	{
		return this._daysOfWeek;
	}

	public int getStartHour()
	{
		return this._startHour;
	}

	public int getStartMinute()
	{
		return this._startMinute;
	}

	public int getStopHour()
	{
		return this._stopHour;
	}

	public int getStopMinute()
	{
		return this._stopMinute;
	}

	public int getStock()
	{
		return this._stock;
	}

	public int getTotal()
	{
		return this._maxStock;
	}

	public int getSalePercent()
	{
		return this._salePercent;
	}

	public int getMinLevel()
	{
		return this._minLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public int getMinBirthday()
	{
		return this._minBirthday;
	}

	public int getMaxBirthday()
	{
		return this._maxBirthday;
	}

	public int getAccountDailyLimit()
	{
		return this._accountDailyLimit;
	}

	public int getAccountBuyLimit()
	{
		return this._accountBuyLimit;
	}

	public boolean isVipGift()
	{
		return this._isVipGift;
	}

	public int getVipTier()
	{
		return this._vipTier;
	}

	public List<PrimeShopItem> getItems()
	{
		return this._items;
	}
}
