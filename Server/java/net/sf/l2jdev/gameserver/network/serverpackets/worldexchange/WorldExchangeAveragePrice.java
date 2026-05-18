package net.sf.l2jdev.gameserver.network.serverpackets.worldexchange;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class WorldExchangeAveragePrice extends ServerPacket
{
	private final int _itemId;
	private final long _averagePrice;

	public WorldExchangeAveragePrice(int itemId)
	{
		this._itemId = itemId;
		this._averagePrice = WorldExchangeManager.getInstance().getAveragePriceOfItem(itemId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_AVERAGE_PRICE.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._averagePrice);
	}
}
