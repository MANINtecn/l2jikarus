package net.sf.l2jdev.gameserver.network.serverpackets.attendance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExVipAttendanceCheck extends ServerPacket
{
	private final boolean _available;

	public ExVipAttendanceCheck(boolean available)
	{
		this._available = available;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VIP_ATTENDANCE_CHECK.writeId(this, buffer);
		buffer.writeByte(this._available);
	}
}
