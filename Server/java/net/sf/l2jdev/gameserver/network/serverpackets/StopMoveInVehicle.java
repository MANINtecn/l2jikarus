package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class StopMoveInVehicle extends ServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final Location _pos;
	private final int _heading;

	public StopMoveInVehicle(Player player, int boatId)
	{
		this._objectId = player.getObjectId();
		this._boatId = boatId;
		this._pos = player.getInVehiclePosition();
		this._heading = player.getHeading();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.STOP_MOVE_IN_VEHICLE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._boatId);
		buffer.writeInt(this._pos.getX());
		buffer.writeInt(this._pos.getY());
		buffer.writeInt(this._pos.getZ());
		buffer.writeInt(this._heading);
	}
}
