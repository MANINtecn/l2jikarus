package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class DoorInfo extends ServerPacket
{
	private final Door _door;

	public DoorInfo(Door door)
	{
		this._door = door;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.DOOR_INFO.writeId(this, buffer);
		buffer.writeInt(this._door.getObjectId());
		buffer.writeInt(this._door.getId());
	}
}
