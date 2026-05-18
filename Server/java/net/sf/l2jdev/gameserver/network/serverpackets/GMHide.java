package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GMHide extends ServerPacket
{
	private final int _mode;

	public GMHide(int mode)
	{
		this._mode = mode;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GM_HIDE.writeId(this, buffer);
		buffer.writeInt(this._mode);
	}
}
