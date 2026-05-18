package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameChangePoints extends ServerPacket
{
	private final int _timeLeft;
	private final int _bluePoints;
	private final int _redPoints;

	public ExCubeGameChangePoints(int timeLeft, int bluePoints, int redPoints)
	{
		this._timeLeft = timeLeft;
		this._bluePoints = bluePoints;
		this._redPoints = redPoints;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_STATE.writeId(this, buffer);
		buffer.writeInt(2);
		buffer.writeInt(this._timeLeft);
		buffer.writeInt(this._bluePoints);
		buffer.writeInt(this._redPoints);
	}
}
