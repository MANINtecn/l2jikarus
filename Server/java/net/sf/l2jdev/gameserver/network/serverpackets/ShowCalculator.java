package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShowCalculator extends ServerPacket
{
	private final int _calculatorId;

	public ShowCalculator(int calculatorId)
	{
		this._calculatorId = calculatorId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHOW_CALC.writeId(this, buffer);
		buffer.writeInt(this._calculatorId);
	}
}
