package net.sf.l2jdev.gameserver.network.serverpackets.storereview;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.PrivateStoreHistoryManager;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class RequestPrivateStoreSearchStatistics extends AbstractItemPacket
{
	private final List<PrivateStoreHistoryManager.ItemHistoryTransaction> _mostItems;
	private final List<PrivateStoreHistoryManager.ItemHistoryTransaction> _highestItems;

	public RequestPrivateStoreSearchStatistics()
	{
		PrivateStoreHistoryManager manager = PrivateStoreHistoryManager.getInstance();
		this._mostItems = manager.getTopMostItem();
		this._highestItems = manager.getTopHighestItem();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PRIVATE_STORE_SEARCH_STATISTICS.writeId(this, buffer);
		buffer.writeInt(Math.min(this._mostItems.size(), 5));

		for (int i = 0; i < Math.min(this._mostItems.size(), 5); i++)
		{
			buffer.writeInt((int) this._mostItems.get(i).getCount());
			ItemInfo itemInfo = new ItemInfo(new Item(this._mostItems.get(i).getItemId()));
			buffer.writeInt(this.calculatePacketSize(itemInfo));
			this.writeItem(itemInfo, this._mostItems.get(i).getCount(), buffer);
		}

		buffer.writeInt(Math.min(this._highestItems.size(), 5));

		for (int i = 0; i < Math.min(this._highestItems.size(), 5); i++)
		{
			buffer.writeLong(this._highestItems.get(i).getPrice());
			ItemInfo itemInfo = new ItemInfo(new Item(this._highestItems.get(i).getItemId()));
			buffer.writeInt(this.calculatePacketSize(itemInfo));
			this.writeItem(itemInfo, this._highestItems.get(i).getCount(), buffer);
		}
	}
}
