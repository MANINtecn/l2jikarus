package net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExDivideAdenaStart extends ServerPacket
{
	public static final ExDivideAdenaStart STATIC_PACKET = new ExDivideAdenaStart();

	private ExDivideAdenaStart()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DIVIDE_ADENA_START.writeId(this, buffer);
	}
}
