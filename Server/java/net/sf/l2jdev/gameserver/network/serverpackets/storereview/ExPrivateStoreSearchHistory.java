package net.sf.l2jdev.gameserver.network.serverpackets.storereview;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.PrivateStoreHistoryManager;
import net.sf.l2jdev.gameserver.managers.PrivateStoreHistoryManager.ItemHistoryTransaction;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExPrivateStoreSearchHistory extends AbstractItemPacket
{
	private final int _page;
	private final int _maxPage;
	private final List<PrivateStoreHistoryManager.ItemHistoryTransaction> _history;

	public ExPrivateStoreSearchHistory(int page, int maxPage, List<PrivateStoreHistoryManager.ItemHistoryTransaction> history)
	{
		this._page = page;
		this._maxPage = maxPage;
		this._history = history;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PRIVATE_STORE_SEARCH_HISTORY.writeId(this, buffer);
		buffer.writeByte(this._page);
		buffer.writeByte(this._maxPage);
		buffer.writeInt(this._history.size());

		for (ItemHistoryTransaction transaction : this._history)
		{
			buffer.writeInt(transaction.getItemId());
			buffer.writeByte(transaction.getTransactionType() == PrivateStoreType.SELL ? 0 : 1);
			buffer.writeByte(transaction.getEnchantLevel());
			buffer.writeLong(transaction.getPrice() / transaction.getCount());
			buffer.writeLong(transaction.getCount());
		}
	}
}
