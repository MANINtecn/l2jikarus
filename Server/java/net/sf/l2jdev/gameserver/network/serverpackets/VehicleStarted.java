package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class VehicleStarted extends ServerPacket
{
	private final int _objectId;
	private final int _state;

	public VehicleStarted(Creature boat, int state)
	{
		this._objectId = boat.getObjectId();
		this._state = state;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VEHICLE_START_PACKET.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._state);
	}
}
