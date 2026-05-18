package net.sf.l2jdev.gameserver.network.serverpackets.adenadistribution;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExDivideAdenaCancel extends ServerPacket
{
	public static final ExDivideAdenaCancel STATIC_PACKET = new ExDivideAdenaCancel();

	private ExDivideAdenaCancel()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DIVIDE_ADENA_CANCEL.writeId(this, buffer);
		buffer.writeByte(0);
	}
}
