package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRegenMax extends ServerPacket
{
	private final int _time;
	private final int _tickInterval;
	private final double _amountPerTick;

	public ExRegenMax(int time, int tickInterval, double amountPerTick)
	{
		this._time = time;
		this._tickInterval = tickInterval;
		this._amountPerTick = amountPerTick;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REGEN_MAX.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(this._time);
		buffer.writeInt(this._tickInterval);
		buffer.writeDouble(this._amountPerTick);
	}
}
