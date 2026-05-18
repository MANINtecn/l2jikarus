package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.instance.Boat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class VehicleInfo extends ServerPacket
{
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public VehicleInfo(Boat boat)
	{
		this._objId = boat.getObjectId();
		this._x = boat.getX();
		this._y = boat.getY();
		this._z = boat.getZ();
		this._heading = boat.getHeading();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VEHICLE_INFO.writeId(this, buffer);
		buffer.writeInt(this._objId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(this._heading);
	}
}
