package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBirthdayPopup extends ServerPacket
{
	public static final ExBirthdayPopup STATIC_PACKET = new ExBirthdayPopup();

	private ExBirthdayPopup()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NOTIFY_BIRTHDAY.writeId(this, buffer);
	}
}
