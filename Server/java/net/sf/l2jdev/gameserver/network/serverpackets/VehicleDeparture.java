package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.instance.Boat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class VehicleDeparture extends ServerPacket
{
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _moveSpeed;
	private final int _rotationSpeed;

	public VehicleDeparture(Boat boat)
	{
		this._objId = boat.getObjectId();
		this._x = boat.getXdestination();
		this._y = boat.getYdestination();
		this._z = boat.getZdestination();
		this._moveSpeed = (int) boat.getMoveSpeed();
		this._rotationSpeed = (int) boat.getStat().getRotationSpeed();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VEHICLE_DEPARTURE.writeId(this, buffer);
		buffer.writeInt(this._objId);
		buffer.writeInt(this._moveSpeed);
		buffer.writeInt(this._rotationSpeed);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
	}
}
