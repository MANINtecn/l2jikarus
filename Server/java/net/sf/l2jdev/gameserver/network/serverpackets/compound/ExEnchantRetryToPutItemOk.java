package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantRetryToPutItemOk extends ServerPacket
{
	public static final ExEnchantRetryToPutItemOk STATIC_PACKET = new ExEnchantRetryToPutItemOk();

	private ExEnchantRetryToPutItemOk()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_RETRY_TO_PUT_ITEMS_OK.writeId(this, buffer);
	}
}
