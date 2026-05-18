package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TargetSelected extends ServerPacket
{
	private final int _objectId;
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;

	public TargetSelected(int objectId, int targetId, int x, int y, int z)
	{
		this._objectId = objectId;
		this._targetObjId = targetId;
		this._x = x;
		this._y = y;
		this._z = z;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TARGET_SELECTED.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._targetObjId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(0);
	}
}
