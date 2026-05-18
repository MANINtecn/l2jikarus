package net.sf.l2jdev.gameserver.network.serverpackets.attributechange;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExChangeAttributeFail extends ServerPacket
{
	public static final ExChangeAttributeFail STATIC = new ExChangeAttributeFail();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_ATTRIBUTE_FAIL.writeId(this, buffer);
	}
}
