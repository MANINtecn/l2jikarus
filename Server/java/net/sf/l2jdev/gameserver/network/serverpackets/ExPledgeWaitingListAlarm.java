package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeWaitingListAlarm extends ServerPacket
{
	public static final ExPledgeWaitingListAlarm STATIC_PACKET = new ExPledgeWaitingListAlarm();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_WAITING_LIST_ALARM.writeId(this, buffer);
	}
}
