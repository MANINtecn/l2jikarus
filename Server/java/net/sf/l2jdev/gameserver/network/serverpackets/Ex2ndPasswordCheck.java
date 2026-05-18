package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Ex2ndPasswordCheck extends ServerPacket
{
	public static final int PASSWORD_NEW = 0;
	public static final int PASSWORD_PROMPT = 1;
	public static final int PASSWORD_OK = 2;
	private final int _windowType;

	public Ex2ndPasswordCheck(int windowType)
	{
		this._windowType = windowType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_2ND_PASSWORD_CHECK.writeId(this, buffer);
		buffer.writeInt(this._windowType);
		buffer.writeInt(0);
	}
}
