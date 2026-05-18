package net.sf.l2jdev.gameserver.model.itemcontainer;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanWHItemAdd;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanWHItemDestroy;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerClanWHItemTransfer;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class ClanWarehouse extends Warehouse
{
	private final Clan _clan;

	public ClanWarehouse(Clan clan)
	{
		this._clan = clan;
	}

	@Override
	public String getName()
	{
		return "ClanWarehouse";
	}

	@Override
	public int getOwnerId()
	{
		return this._clan.getId();
	}

	@Override
	public Player getOwner()
	{
		return this._clan.getLeader().getPlayer();
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return this._items.size() + slots <= PlayerConfig.WAREHOUSE_SLOTS_CLAN;
	}

	@Override
	public Item addItem(ItemProcessType process, int itemId, long count, Player actor, Object reference)
	{
		Item item = super.addItem(process, itemId, count, actor, reference);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_ADD, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemAdd(actor, item, this), item.getTemplate());
		}

		return item;
	}

	@Override
	public Item addItem(ItemProcessType process, Item item, Player actor, Object reference)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_ADD, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemAdd(actor, item, this), item.getTemplate());
		}

		return super.addItem(process, item, actor, reference);
	}

	@Override
	public Item destroyItem(ItemProcessType process, Item item, long count, Player actor, Object reference)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_DESTROY, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemDestroy(actor, item, count, this), item.getTemplate());
		}

		return super.destroyItem(process, item, count, actor, reference);
	}

	@Override
	public Item transferItem(ItemProcessType process, int objectId, long count, ItemContainer target, Player actor, Object reference)
	{
		Item item = this.getItemByObjectId(objectId);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_TRANSFER, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemTransfer(actor, item, count, target), item.getTemplate());
		}

		return super.transferItem(process, objectId, count, target, actor, reference);
	}
}
