package net.sf.l2jdev.gameserver.network.serverpackets.autopeel;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExStopItemAutoPeel extends ServerPacket
{
	private final boolean _result;

	public ExStopItemAutoPeel(boolean result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STOP_ITEM_AUTO_PEEL.writeId(this, buffer);
		buffer.writeByte(this._result);
	}
}
