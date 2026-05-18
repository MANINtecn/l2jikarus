package net.sf.l2jdev.gameserver.network.serverpackets.worldexchange;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class WorldExchangeSellCompleteAlarm extends ServerPacket
{
	private final int _itemId;
	private final long _amount;

	public WorldExchangeSellCompleteAlarm(int itemId, long amount)
	{
		this._itemId = itemId;
		this._amount = amount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_SELL_COMPLETE_ALARM.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._amount);
	}
}
