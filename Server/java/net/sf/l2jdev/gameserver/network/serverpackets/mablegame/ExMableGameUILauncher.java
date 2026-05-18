package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGameUILauncher extends ServerPacket
{
	public static final ExMableGameUILauncher STATIC_PACKET = new ExMableGameUILauncher();

	private ExMableGameUILauncher()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_UI_LAUNCHER.writeId(this, buffer);
		buffer.writeByte(1);
	}
}
