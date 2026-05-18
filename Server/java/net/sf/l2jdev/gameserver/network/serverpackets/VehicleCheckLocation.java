package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class VehicleCheckLocation extends ServerPacket
{
	private final Creature _boat;

	public VehicleCheckLocation(Creature boat)
	{
		this._boat = boat;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VEHICLE_CHECK_LOCATION.writeId(this, buffer);
		buffer.writeInt(this._boat.getObjectId());
		buffer.writeInt(this._boat.getX());
		buffer.writeInt(this._boat.getY());
		buffer.writeInt(this._boat.getZ());
		buffer.writeInt(this._boat.getHeading());
	}
}
