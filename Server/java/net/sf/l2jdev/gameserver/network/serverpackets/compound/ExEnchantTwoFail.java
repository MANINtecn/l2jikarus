package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantTwoFail extends ServerPacket
{
	public static final ExEnchantTwoFail STATIC_PACKET = new ExEnchantTwoFail();

	private ExEnchantTwoFail()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_TWO_FAIL.writeId(this, buffer);
	}
}
