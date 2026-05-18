package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class MoveToLocationInVehicle extends ServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final Location _destination;
	private final Location _origin;

	public MoveToLocationInVehicle(Player player, Location destination, Location origin)
	{
		this._objectId = player.getObjectId();
		this._boatId = player.getBoat().getObjectId();
		this._destination = destination;
		this._origin = origin;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MOVE_TO_LOCATION_IN_VEHICLE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._boatId);
		buffer.writeInt(this._destination.getX());
		buffer.writeInt(this._destination.getY());
		buffer.writeInt(this._destination.getZ());
		buffer.writeInt(this._origin.getX());
		buffer.writeInt(this._origin.getY());
		buffer.writeInt(this._origin.getZ());
	}
}
