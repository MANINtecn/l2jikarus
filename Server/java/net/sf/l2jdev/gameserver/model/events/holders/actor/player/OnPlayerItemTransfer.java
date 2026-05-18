package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;

public class OnPlayerItemTransfer implements IBaseEvent
{
	private final Player _player;
	private final Item _item;
	private final ItemContainer _container;

	public OnPlayerItemTransfer(Player player, Item item, ItemContainer container)
	{
		this._player = player;
		this._item = item;
		this._container = container;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Item getItem()
	{
		return this._item;
	}

	public ItemContainer getContainer()
	{
		return this._container;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_ITEM_TRANSFER;
	}
}
