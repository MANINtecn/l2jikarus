package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GetOnVehicle extends ServerPacket
{
	private final int _objectId;
	private final int _boatObjId;
	private final Location _pos;

	public GetOnVehicle(int charObjId, int boatObjId, Location pos)
	{
		this._objectId = charObjId;
		this._boatObjId = boatObjId;
		this._pos = pos;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GETON_VEHICLE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._boatObjId);
		buffer.writeInt(this._pos.getX());
		buffer.writeInt(this._pos.getY());
		buffer.writeInt(this._pos.getZ());
	}
}
