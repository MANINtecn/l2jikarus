package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GetOffVehicle extends ServerPacket
{
	private final int _objectId;
	private final int _boatObjId;
	private final int _x;
	private final int _y;
	private final int _z;

	public GetOffVehicle(int charObjId, int boatObjId, int x, int y, int z)
	{
		this._objectId = charObjId;
		this._boatObjId = boatObjId;
		this._x = x;
		this._y = y;
		this._z = z;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GETOFF_VEHICLE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._boatObjId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
	}
}
