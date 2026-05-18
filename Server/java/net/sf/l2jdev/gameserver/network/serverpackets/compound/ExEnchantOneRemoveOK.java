package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantOneRemoveOK extends ServerPacket
{
	public static final ExEnchantOneRemoveOK STATIC_PACKET = new ExEnchantOneRemoveOK();

	private ExEnchantOneRemoveOK()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_ONE_REMOVE_OK.writeId(this, buffer);
	}
}
