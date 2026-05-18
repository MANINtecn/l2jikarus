package net.sf.l2jdev.gameserver.network.serverpackets.secretshop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFestivalBmInfo extends ServerPacket
{
	private final int _itemId;
	private final int _itemAmount;
	private final int _itemAmountPerGame;

	public ExFestivalBmInfo(int itemId, int itemAmount, int itemAmountPerGame)
	{
		this._itemId = itemId;
		this._itemAmount = itemAmount;
		this._itemAmountPerGame = itemAmountPerGame;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FESTIVAL_BM_INFO.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._itemAmount);
		buffer.writeInt(this._itemAmountPerGame);
	}
}
