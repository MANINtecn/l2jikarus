package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class DeleteObject extends ServerPacket
{
	private final int _objectId;

	public DeleteObject(WorldObject obj)
	{
		this._objectId = obj.getObjectId();
	}

	public DeleteObject(int objectId)
	{
		this._objectId = objectId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.DELETE_OBJECT.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeByte(0);
	}
}
