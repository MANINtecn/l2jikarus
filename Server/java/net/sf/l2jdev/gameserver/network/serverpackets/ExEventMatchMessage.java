package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExEventMatchMessage extends ServerPacket
{
	private final int _type;
	private final String _message;

	public ExEventMatchMessage(int type, String message)
	{
		this._type = type;
		this._message = message;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_EVENT_MATCH_MESSAGE.writeId(this, buffer);
		buffer.writeByte(this._type);
		buffer.writeString(this._message);
	}
}
