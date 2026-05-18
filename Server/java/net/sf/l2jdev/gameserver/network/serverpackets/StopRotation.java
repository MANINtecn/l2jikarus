package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class StopRotation extends ServerPacket
{
	private final int _objectId;
	private final int _degree;
	private final int _speed;

	public StopRotation(int objectId, int degree, int speed)
	{
		this._objectId = objectId;
		this._degree = degree;
		this._speed = speed;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FINISH_ROTATING.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._degree);
		buffer.writeInt(this._speed);
		buffer.writeInt(0);
	}
}
