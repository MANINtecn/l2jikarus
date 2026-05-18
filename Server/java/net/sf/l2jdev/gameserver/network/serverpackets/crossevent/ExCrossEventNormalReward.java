package net.sf.l2jdev.gameserver.network.serverpackets.crossevent;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCrossEventNormalReward extends ServerPacket
{
	private final int _vertical;
	private final int _horizontal;
	private final int _rewardAmount;

	public ExCrossEventNormalReward(int vertical, int horizontal, int rewardAmount)
	{
		this._vertical = vertical;
		this._horizontal = horizontal;
		this._rewardAmount = rewardAmount;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CROSS_EVENT_NORMAL_REWARD.writeId(this, buffer);
		buffer.writeByte(1);
		buffer.writeInt(this._vertical);
		buffer.writeInt(this._horizontal);
		buffer.writeInt(this._rewardAmount);
	}
}
