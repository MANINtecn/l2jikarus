package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShowTownMap extends ServerPacket
{
	private final String _texture;
	private final int _x;
	private final int _y;

	public ShowTownMap(String texture, int x, int y)
	{
		this._texture = texture;
		this._x = x;
		this._y = y;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHOW_TOWNMAP.writeId(this, buffer);
		buffer.writeString(this._texture);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
	}
}
