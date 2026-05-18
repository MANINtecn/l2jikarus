package net.sf.l2jdev.gameserver.network.serverpackets.raidbossinfo;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRaidServerInfo extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RAID_SERVER_INFO.writeId(this, buffer);
	}
}
