package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TargetUnselected extends ServerPacket
{
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;

	public TargetUnselected(Creature creature)
	{
		this._targetObjId = creature.getObjectId();
		this._x = creature.getX();
		this._y = creature.getY();
		this._z = creature.getZ();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TARGET_UNSELECTED.writeId(this, buffer);
		buffer.writeInt(this._targetObjId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(0);
	}
}
