package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TeleportToLocation extends ServerPacket
{
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public TeleportToLocation(WorldObject obj, int x, int y, int z, int heading)
	{
		this._targetObjId = obj.getObjectId();
		this._x = x;
		this._y = y;
		this._z = z;
		this._heading = heading;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TELEPORT_TO_LOCATION.writeId(this, buffer);
		buffer.writeInt(this._targetObjId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(0);
		buffer.writeInt(this._heading);
		buffer.writeInt(0);
	}
}
