package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadInfo extends ServerPacket
{
	private final int _open;
	private final int _remainTime;

	public ExOlympiadInfo(int open, int remainTime)
	{
		this._open = open;
		this._remainTime = remainTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_INFO.writeId(this, buffer);
		buffer.writeByte(this._open);
		buffer.writeInt(this._remainTime);
		buffer.writeByte(1);
	}
}
