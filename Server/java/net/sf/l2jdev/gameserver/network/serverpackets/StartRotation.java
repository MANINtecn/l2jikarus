package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class StartRotation extends ServerPacket
{
	private final int _objectId;
	private final int _degree;
	private final int _side;
	private final int _speed;

	public StartRotation(int objectId, int degree, int side, int speed)
	{
		this._objectId = objectId;
		this._degree = degree;
		this._side = side;
		this._speed = speed;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.START_ROTATING.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._degree);
		buffer.writeInt(this._side);
		buffer.writeInt(this._speed);
	}
}
