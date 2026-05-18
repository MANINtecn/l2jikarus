package net.sf.l2jdev.gameserver.model.itemauction;

import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class AuctionItem
{
	private final int _auctionItemId;
	private final int _auctionLength;
	private final long _auctionInitBid;
	private final int _itemId;
	private final long _itemCount;
	public final StatSet _itemExtra;

	public AuctionItem(int auctionItemId, int auctionLength, long auctionInitBid, int itemId, long itemCount, StatSet itemExtra)
	{
		this._auctionItemId = auctionItemId;
		this._auctionLength = auctionLength;
		this._auctionInitBid = auctionInitBid;
		this._itemId = itemId;
		this._itemCount = itemCount;
		this._itemExtra = itemExtra;
	}

	public boolean checkItemExists()
	{
		return ItemData.getInstance().getTemplate(this._itemId) != null;
	}

	public int getAuctionItemId()
	{
		return this._auctionItemId;
	}

	public int getAuctionLength()
	{
		return this._auctionLength;
	}

	public long getAuctionInitBid()
	{
		return this._auctionInitBid;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public long getItemCount()
	{
		return this._itemCount;
	}

	public Item createNewItemInstance()
	{
		Item item = new Item(IdManager.getInstance().getNextId(), this._itemId);
		World.getInstance().addObject(item);
		item.setCount(this._itemCount);
		item.setEnchantLevel(item.getTemplate().getDefaultEnchantLevel());
		return item;
	}
}
