package net.sf.l2jdev.gameserver.model.item.henna;

import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class DyePotentialFee
{
	private final int _step;
	private final List<ItemHolder> _items;
	private final int _dailyCount;
	private final Map<Integer, Double> _enchantExp;
	private final long _adenaFee;

	public DyePotentialFee(int step, List<ItemHolder> items, long adenaFee, int dailyCount, Map<Integer, Double> enchantExp)
	{
		this._step = step;
		this._items = items;
		this._adenaFee = adenaFee;
		this._dailyCount = dailyCount;
		this._enchantExp = enchantExp;
	}

	public int getStep()
	{
		return this._step;
	}

	public List<ItemHolder> getItems()
	{
		return this._items;
	}

	public long getAdenaFee()
	{
		return this._adenaFee;
	}

	public int getDailyCount()
	{
		return this._dailyCount;
	}

	public Map<Integer, Double> getEnchantExp()
	{
		return this._enchantExp;
	}
}
