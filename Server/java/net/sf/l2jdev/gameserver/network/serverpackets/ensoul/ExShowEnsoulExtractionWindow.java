package net.sf.l2jdev.gameserver.network.serverpackets.ensoul;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowEnsoulExtractionWindow extends ServerPacket
{
	public static final ExShowEnsoulExtractionWindow STATIC_PACKET = new ExShowEnsoulExtractionWindow();

	private ExShowEnsoulExtractionWindow()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_ENSOUL_EXTRACTION_WINDOW.writeId(this, buffer);
	}
}
