package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantTwoOK extends ServerPacket
{
	public static final ExEnchantTwoOK STATIC_PACKET = new ExEnchantTwoOK();

	private ExEnchantTwoOK()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_TWO_OK.writeId(this, buffer);
		buffer.writeInt(0);
	}
}
