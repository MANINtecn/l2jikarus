package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Revive extends ServerPacket
{
	private final int _objectId;

	public Revive(WorldObject obj)
	{
		this._objectId = obj.getObjectId();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.REVIVE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
	}
}
