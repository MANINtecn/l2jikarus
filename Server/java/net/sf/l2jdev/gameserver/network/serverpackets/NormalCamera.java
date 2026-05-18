package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class NormalCamera extends ServerPacket
{
	public static final NormalCamera STATIC_PACKET = new NormalCamera();

	private NormalCamera()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.NORMAL_CAMERA.writeId(this, buffer);
	}
}
