package net.sf.l2jdev.gameserver.model.events.holders.item;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;

public class OnMultisellBuyItem implements IBaseEvent
{
	private final Player _player;
	private final long _multisellId;
	private final long _amount;
	private final List<ItemChanceHolder> _resourceItems;
	private final List<ItemChanceHolder> _boughtItems;

	public OnMultisellBuyItem(Player player, long multisellId, long amount, List<ItemChanceHolder> resourceItems, List<ItemChanceHolder> boughtItems)
	{
		this._player = player;
		this._multisellId = multisellId;
		this._amount = amount;
		this._resourceItems = resourceItems;
		this._boughtItems = boughtItems;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public long getMultisellId()
	{
		return this._multisellId;
	}

	public long getAmount()
	{
		return this._amount;
	}

	public List<ItemChanceHolder> getResourceItems()
	{
		return this._resourceItems;
	}

	public List<ItemChanceHolder> getBoughtItems()
	{
		return this._boughtItems;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_MULTISELL_BUY_ITEM;
	}
}
