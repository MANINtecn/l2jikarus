package net.sf.l2jdev.gameserver.network.serverpackets.classchange;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExClassChangeSetAlarm extends ServerPacket
{
	public static final ExClassChangeSetAlarm STATIC_PACKET = new ExClassChangeSetAlarm();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CLASS_CHANGE_SET_ALARM.writeId(this, buffer);
	}
}
