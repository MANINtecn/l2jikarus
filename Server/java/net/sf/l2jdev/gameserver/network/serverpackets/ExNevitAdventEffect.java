package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNevitAdventEffect extends ServerPacket
{
	private final int _timeLeft;

	public ExNevitAdventEffect(int timeLeft)
	{
		this._timeLeft = timeLeft;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_CHANNELING_EFFECT.writeId(this, buffer);
		buffer.writeInt(this._timeLeft);
	}
}
