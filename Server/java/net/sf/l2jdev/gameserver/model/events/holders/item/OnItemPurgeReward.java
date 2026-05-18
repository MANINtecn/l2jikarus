package net.sf.l2jdev.gameserver.model.events.holders.item;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class OnItemPurgeReward implements IBaseEvent
{
	private final Player _player;
	private final Item _item;

	public OnItemPurgeReward(Player player, Item item)
	{
		this._player = player;
		this._item = item;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Item getItem()
	{
		return this._item;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ITEM_PURGE_REWARD;
	}
}
