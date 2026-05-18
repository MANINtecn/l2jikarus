package net.sf.l2jdev.gameserver.network.serverpackets.teleports;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowTeleportUi extends ServerPacket
{
	public static final ExShowTeleportUi STATIC_PACKET = new ExShowTeleportUi();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_TELEPORT_UI.writeId(this, buffer);
	}
}
