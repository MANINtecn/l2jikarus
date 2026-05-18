package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class MoveToLocation extends ServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _xDst;
	private final int _yDst;
	private final int _zDst;

	public MoveToLocation(Creature creature)
	{
		this._objectId = creature.getObjectId();
		this._x = creature.getX();
		this._y = creature.getY();
		this._z = creature.getZ();
		this._xDst = creature.getXdestination();
		this._yDst = creature.getYdestination();
		this._zDst = creature.getZdestination();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MOVE_TO_LOCATION.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._xDst);
		buffer.writeInt(this._yDst);
		buffer.writeInt(this._zDst);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
	}

	@Override
	public boolean canBeDropped(GameClient client)
	{
		return true;
	}
}
