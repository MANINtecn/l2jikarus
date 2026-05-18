package net.sf.l2jdev.gameserver.network.serverpackets.ensoul;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowEnsoulWindow extends ServerPacket
{
	public static final ExShowEnsoulWindow STATIC_PACKET = new ExShowEnsoulWindow();

	private ExShowEnsoulWindow()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_ENSOUL_WINDOW.writeId(this, buffer);
	}
}
