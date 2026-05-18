package net.sf.l2jdev.gameserver.model.script.newquestdata;

import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class NewQuestReward
{
	private final long _exp;
	private final long _sp;
	private final int _level;
	private final List<ItemHolder> _items;

	public NewQuestReward(long exp, long sp, int level, List<ItemHolder> items)
	{
		this._exp = exp;
		this._sp = sp;
		this._level = level;
		this._items = items;
	}

	public long getExp()
	{
		return this._exp;
	}

	public long getSp()
	{
		return this._sp;
	}

	public int getLevel()
	{
		return this._level;
	}

	public List<ItemHolder> getItems()
	{
		return this._items;
	}
}
