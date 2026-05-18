package net.sf.l2jdev.gameserver.network.serverpackets.worldexchange;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class WorldExchangeSettleRecvResult extends ServerPacket
{
	public static final WorldExchangeSettleRecvResult FAIL = new WorldExchangeSettleRecvResult(-1, -1L, (byte) 0);
	private final int _itemObjectId;
	private final long _itemAmount;
	private final byte _type;

	public WorldExchangeSettleRecvResult(int itemObjectId, long itemAmount, byte type)
	{
		this._itemObjectId = itemObjectId;
		this._itemAmount = itemAmount;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_SETTLE_RECV_RESULT.writeId(this, buffer);
		buffer.writeInt(this._itemObjectId);
		buffer.writeLong(this._itemAmount);
		buffer.writeByte(this._type);
	}
}
