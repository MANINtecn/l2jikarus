package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPlayScene extends ServerPacket
{
	public static final ExPlayScene STATIC_PACKET = new ExPlayScene();

	private ExPlayScene()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLAY_SCENE.writeId(this, buffer);
	}
}
