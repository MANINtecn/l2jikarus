package net.sf.l2jdev.gameserver.network.serverpackets.attendance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExVipAttendanceNotify extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VIP_ATTENDANCE_NOTIFY.writeId(this, buffer);
		buffer.writeByte(1);
	}
}
