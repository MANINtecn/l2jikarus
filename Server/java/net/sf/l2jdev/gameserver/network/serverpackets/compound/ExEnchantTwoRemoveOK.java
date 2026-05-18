package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantTwoRemoveOK extends ServerPacket
{
	public static final ExEnchantTwoRemoveOK STATIC_PACKET = new ExEnchantTwoRemoveOK();

	private ExEnchantTwoRemoveOK()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_TWO_REMOVE_OK.writeId(this, buffer);
	}
}
