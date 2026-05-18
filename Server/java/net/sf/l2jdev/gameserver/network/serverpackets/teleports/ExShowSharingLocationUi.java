package net.sf.l2jdev.gameserver.network.serverpackets.teleports;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowSharingLocationUi extends ServerPacket
{
	public static final ExShowSharingLocationUi STATIC_PACKET = new ExShowSharingLocationUi();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHARED_POSITION_SHARING_UI.writeId(this, buffer);
		buffer.writeLong(GeneralConfig.SHARING_LOCATION_COST);
	}
}
