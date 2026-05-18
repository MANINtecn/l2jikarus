package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SetSummonRemainTime extends ServerPacket
{
	private final int _maxTime;
	private final int _remainingTime;

	public SetSummonRemainTime(int maxTime, int remainingTime)
	{
		this._remainingTime = remainingTime;
		this._maxTime = maxTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SET_SUMMON_REMAIN_TIME.writeId(this, buffer);
		buffer.writeInt(this._maxTime);
		buffer.writeInt(this._remainingTime);
	}
}
