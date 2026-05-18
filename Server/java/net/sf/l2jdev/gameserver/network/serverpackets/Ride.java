package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Ride extends ServerPacket
{
	private final int _objectId;
	private final boolean _mounted;
	private final int _rideType;
	private final int _rideNpcId;
	private final Location _loc;

	public Ride(Player player)
	{
		this._objectId = player.getObjectId();
		this._mounted = player.isMounted();
		this._rideType = player.getMountType().ordinal();
		this._rideNpcId = player.getMountNpcId() + 1000000;
		this._loc = player.getLocation();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.RIDE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._mounted);
		buffer.writeInt(this._rideType);
		buffer.writeInt(this._rideNpcId);
		buffer.writeInt(this._loc.getX());
		buffer.writeInt(this._loc.getY());
		buffer.writeInt(this._loc.getZ());
	}
}
