package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.commission.CommissionItem;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExResponseCommissionList extends AbstractItemPacket
{
	public static final int MAX_CHUNK_SIZE = 120;
	private final ExResponseCommissionList.CommissionListReplyType _replyType;
	private final List<CommissionItem> _items;
	private final int _chunkId;
	private final int _listIndexStart;

	public ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType replyType)
	{
		this(replyType, Collections.emptyList(), 0);
	}

	public ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType replyType, List<CommissionItem> items)
	{
		this(replyType, items, 0);
	}

	public ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType replyType, List<CommissionItem> items, int chunkId)
	{
		this(replyType, items, chunkId, 0);
	}

	public ExResponseCommissionList(ExResponseCommissionList.CommissionListReplyType replyType, List<CommissionItem> items, int chunkId, int listIndexStart)
	{
		this._replyType = replyType;
		this._items = items;
		this._chunkId = chunkId;
		this._listIndexStart = listIndexStart;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_COMMISSION_LIST.writeId(this, buffer);
		buffer.writeInt(this._replyType.getClientId());
		switch (this._replyType)
		{
			case PLAYER_AUCTIONS:
			case AUCTIONS:
				buffer.writeInt((int) Instant.now().getEpochSecond());
				buffer.writeInt(this._chunkId);
				int chunkSize = this._items.size() - this._listIndexStart;
				if (chunkSize > 120)
				{
					chunkSize = 120;
				}

				buffer.writeInt(chunkSize);

				for (int i = this._listIndexStart; i < this._listIndexStart + chunkSize; i++)
				{
					CommissionItem commissionItem = this._items.get(i);
					buffer.writeLong(commissionItem.getCommissionId());
					buffer.writeLong(commissionItem.getPricePerUnit());
					buffer.writeInt(0);
					buffer.writeInt((commissionItem.getDurationInDays() - 1) / 2);
					buffer.writeInt((int) commissionItem.getEndTime().getEpochSecond());
					buffer.writeString(null);
					this.writeItem(commissionItem.getItemInfo(), buffer);
				}
		}
	}

	public static enum CommissionListReplyType
	{
		PLAYER_AUCTIONS_EMPTY(-2),
		ITEM_DOES_NOT_EXIST(-1),
		PLAYER_AUCTIONS(2),
		AUCTIONS(3);

		private final int _clientId;

		private CommissionListReplyType(int clientId)
		{
			this._clientId = clientId;
		}

		public int getClientId()
		{
			return this._clientId;
		}
	}
}
