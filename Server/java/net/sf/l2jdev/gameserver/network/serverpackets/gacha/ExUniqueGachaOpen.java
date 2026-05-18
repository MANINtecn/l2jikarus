package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExUniqueGachaOpen extends ServerPacket
{
	private final int _fullInfo;
	private final int _openMode;

	public ExUniqueGachaOpen(int fullInfo, int openMode)
	{
		this._fullInfo = fullInfo;
		this._openMode = openMode;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_OPEN.writeId(this, buffer);
		buffer.writeByte(this._fullInfo);
		buffer.writeInt(this._openMode);
	}
}
