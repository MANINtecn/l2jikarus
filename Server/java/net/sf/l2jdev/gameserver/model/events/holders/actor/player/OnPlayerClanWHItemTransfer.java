package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;

public class OnPlayerClanWHItemTransfer implements IBaseEvent
{
	private final Player _player;
	private final Item _item;
	private final long _count;
	private final ItemContainer _container;

	public OnPlayerClanWHItemTransfer(Player player, Item item, long count, ItemContainer container)
	{
		this._player = player;
		this._item = item;
		this._count = count;
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

	public long getCount()
	{
		return this._count;
	}

	public ItemContainer getContainer()
	{
		return this._container;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_CLAN_WH_ITEM_TRANSFER;
	}
}
