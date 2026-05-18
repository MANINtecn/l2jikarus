package net.sf.l2jdev.gameserver.model.events.holders.item;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class OnItemTalk implements IBaseEvent
{
	private final Item _item;
	private final Player _player;

	public OnItemTalk(Item item, Player player)
	{
		this._item = item;
		this._player = player;
	}

	public Item getItem()
	{
		return this._item;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ITEM_TALK;
	}
}
