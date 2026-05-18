package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExChangeToAwakenedClass extends ServerPacket
{
	private final int _classId;

	public ExChangeToAwakenedClass(int classId)
	{
		this._classId = classId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_TO_AWAKENED_CLASS.writeId(this, buffer);
		buffer.writeInt(this._classId);
	}
}
