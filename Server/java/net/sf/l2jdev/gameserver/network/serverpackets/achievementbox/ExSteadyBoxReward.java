package net.sf.l2jdev.gameserver.network.serverpackets.achievementbox;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSteadyBoxReward extends ServerPacket
{
	private final int _slotId;
	private final int _itemId;
	private final long _itemCount;

	public ExSteadyBoxReward(int slotId, int itemId, long itemCount)
	{
		this._slotId = slotId;
		this._itemId = itemId;
		this._itemCount = itemCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STEADY_BOX_REWARD.writeId(this, buffer);
		buffer.writeInt(this._slotId);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._itemCount);
		buffer.writeInt(0);
	}
}
