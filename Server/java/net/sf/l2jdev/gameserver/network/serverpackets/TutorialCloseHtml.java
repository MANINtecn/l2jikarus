package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TutorialCloseHtml extends ServerPacket
{
	public static final TutorialCloseHtml STATIC_PACKET = new TutorialCloseHtml();

	private TutorialCloseHtml()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TUTORIAL_CLOSE_HTML.writeId(this, buffer);
	}
}
