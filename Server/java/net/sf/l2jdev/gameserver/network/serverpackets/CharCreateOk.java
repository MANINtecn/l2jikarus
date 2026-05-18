package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class CharCreateOk extends ServerPacket
{
	public static final CharCreateOk STATIC_PACKET = new CharCreateOk();

	private CharCreateOk()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHARACTER_CREATE_SUCCESS.writeId(this, buffer);
		buffer.writeInt(1);
	}
}
