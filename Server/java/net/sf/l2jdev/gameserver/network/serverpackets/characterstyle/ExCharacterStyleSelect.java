package net.sf.l2jdev.gameserver.network.serverpackets.characterstyle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCharacterStyleSelect extends ServerPacket
{
	public static final ExCharacterStyleSelect STATIC_PACKET = new ExCharacterStyleSelect();

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHARACTER_STYLE_SELECT.writeId(this, buffer);
		buffer.writeByte(1);
	}
}
