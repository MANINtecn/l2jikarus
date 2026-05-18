package net.sf.l2jdev.gameserver.network.serverpackets.magiclamp;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMagicLampResult extends ServerPacket
{
	private final int _exp;
	private final int _grade;

	public ExMagicLampResult(int exp, int grade)
	{
		this._exp = exp;
		this._grade = grade;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MAGICLAMP_RESULT.writeId(this, buffer);
		buffer.writeLong(this._exp);
		buffer.writeInt(this._grade);
	}
}
