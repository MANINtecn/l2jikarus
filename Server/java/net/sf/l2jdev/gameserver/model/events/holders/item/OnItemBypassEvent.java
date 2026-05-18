package net.sf.l2jdev.gameserver.model.events.holders.item;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class OnItemBypassEvent implements IBaseEvent
{
	private final Item _item;
	private final Player _player;
	private final String _event;

	public OnItemBypassEvent(Item item, Player player, String event)
	{
		this._item = item;
		this._player = player;
		this._event = event;
	}

	public Item getItem()
	{
		return this._item;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public String getEvent()
	{
		return this._event;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ITEM_BYPASS_EVENT;
	}
}
