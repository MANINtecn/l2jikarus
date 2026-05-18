package net.sf.l2jdev.gameserver.network.serverpackets.crossevent;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCrossEventRareReward extends ServerPacket
{
	private final int _itemId;
	private final boolean _isAvailable;

	public ExCrossEventRareReward(boolean isAvailable, int itemId)
	{
		this._isAvailable = isAvailable;
		this._itemId = itemId;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CROSS_EVENT_RARE_REWARD.writeId(this, buffer);
		buffer.writeByte(this._isAvailable);
		buffer.writeLong(this._itemId);
	}
}
