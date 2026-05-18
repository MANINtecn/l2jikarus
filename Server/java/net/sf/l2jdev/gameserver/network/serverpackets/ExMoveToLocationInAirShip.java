package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMoveToLocationInAirShip extends ServerPacket
{
	private final int _objectId;
	private final int _airShipId;
	private final Location _destination;
	private final int _heading;

	public ExMoveToLocationInAirShip(Player player)
	{
		this._objectId = player.getObjectId();
		this._airShipId = player.getAirShip().getObjectId();
		this._destination = player.getInVehiclePosition();
		this._heading = player.getHeading();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MOVE_TO_LOCATION_IN_AIRSHIP.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._airShipId);
		buffer.writeInt(this._destination.getX());
		buffer.writeInt(this._destination.getY());
		buffer.writeInt(this._destination.getZ());
		buffer.writeInt(this._heading);
	}
}
