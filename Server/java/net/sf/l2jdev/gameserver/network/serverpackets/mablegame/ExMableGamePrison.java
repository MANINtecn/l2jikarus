package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGamePrison extends ServerPacket
{
	private final int _minDice;
	private final int _maxDice;
	private final int _remainCount;

	public ExMableGamePrison(int minDice, int maxDice, int remainCount)
	{
		this._minDice = minDice;
		this._maxDice = maxDice;
		this._remainCount = remainCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_PRISON.writeId(this, buffer);
		buffer.writeInt(this._minDice);
		buffer.writeInt(this._maxDice);
		buffer.writeInt(this._remainCount);
	}
}
