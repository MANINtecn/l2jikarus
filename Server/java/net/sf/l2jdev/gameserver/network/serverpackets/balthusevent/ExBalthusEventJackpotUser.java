package net.sf.l2jdev.gameserver.network.serverpackets.balthusevent;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBalthusEventJackpotUser extends ServerPacket
{
	public static final ExBalthusEventJackpotUser STATIC_PACKET = new ExBalthusEventJackpotUser();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALTHUS_JACKPOT_USER.writeId(this, buffer);
	}
}
