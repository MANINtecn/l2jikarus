package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ValidateLocation extends ServerPacket
{
	private final int _objectId;
	private final Location _loc;

	public ValidateLocation(WorldObject obj)
	{
		this._objectId = obj.getObjectId();
		this._loc = obj.getLocation();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VALIDATE_LOCATION.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._loc.getX());
		buffer.writeInt(this._loc.getY());
		buffer.writeInt(this._loc.getZ());
		buffer.writeInt(this._loc.getHeading());
		buffer.writeByte(1);
	}
}
