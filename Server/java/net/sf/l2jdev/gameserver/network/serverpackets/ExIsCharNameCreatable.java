package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExIsCharNameCreatable extends ServerPacket
{
	private final int _allowed;

	public ExIsCharNameCreatable(int allowed)
	{
		this._allowed = allowed;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHECK_CHAR_NAME.writeId(this, buffer);
		buffer.writeInt(this._allowed);
	}
}
