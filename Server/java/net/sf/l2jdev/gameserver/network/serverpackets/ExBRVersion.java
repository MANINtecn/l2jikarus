package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.AttendanceRewardsConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBRVersion extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			ServerPackets.EX_BR_VERSION.writeId(this, buffer);
			buffer.writeByte(0);
			buffer.writeByte(AttendanceRewardsConfig.ENABLE_ATTENDANCE_REWARDS);
			buffer.writeInt(ServerConfig.SERVER_ID);
			buffer.writeByte(0);
		}
	}
}
