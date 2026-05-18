package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExTutorialShowId extends ServerPacket
{
	private final int _id;

	public ExTutorialShowId(int id)
	{
		this._id = id;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TUTORIAL_SHOW_ID.writeId(this, buffer);
		buffer.writeInt(this._id);
	}
}
