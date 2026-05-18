package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPostItemFee extends ServerPacket
{
	private final long _fee;

	public ExPostItemFee(long fee)
	{
		this._fee = fee;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_POST_ITEM_FEE.writeId(this, buffer);
		buffer.writeLong(this._fee);
	}
}
