package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMoveToLocationAirShip extends ServerPacket
{
	private final int _objId;
	private final int _tx;
	private final int _ty;
	private final int _tz;
	private final int _x;
	private final int _y;
	private final int _z;

	public ExMoveToLocationAirShip(Creature creature)
	{
		this._objId = creature.getObjectId();
		this._tx = creature.getXdestination();
		this._ty = creature.getYdestination();
		this._tz = creature.getZdestination();
		this._x = creature.getX();
		this._y = creature.getY();
		this._z = creature.getZ();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MOVE_TO_LOCATION_AIRSHIP.writeId(this, buffer);
		buffer.writeInt(this._objId);
		buffer.writeInt(this._tx);
		buffer.writeInt(this._ty);
		buffer.writeInt(this._tz);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
	}
}
