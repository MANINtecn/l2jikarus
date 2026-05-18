package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExStopMoveAirShip extends ServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public ExStopMoveAirShip(Creature ship)
	{
		this._objectId = ship.getObjectId();
		this._x = ship.getX();
		this._y = ship.getY();
		this._z = ship.getZ();
		this._heading = ship.getHeading();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STOP_MOVE_AIRSHIP.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(this._heading);
	}
}
