package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantRetryToPutItemFail extends ServerPacket
{
	public static final ExEnchantRetryToPutItemFail STATIC_PACKET = new ExEnchantRetryToPutItemFail();

	private ExEnchantRetryToPutItemFail()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_RETRY_TO_PUT_ITEMS_FAIL.writeId(this, buffer);
	}
}
