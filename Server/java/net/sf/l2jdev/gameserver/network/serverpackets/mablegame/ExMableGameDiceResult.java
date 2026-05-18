package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGameDiceResult extends ServerPacket
{
	private final int _dice;
	private final int _resultCellId;
	private final int _resultCellType;
	private final int _remainNormalDiceUseCount;

	public ExMableGameDiceResult(int dice, int resultCellId, int resultCellType, int remainNormalDiceUseCount)
	{
		this._dice = dice;
		this._resultCellId = resultCellId;
		this._resultCellType = resultCellType;
		this._remainNormalDiceUseCount = remainNormalDiceUseCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_DICE_RESULT.writeId(this, buffer);
		buffer.writeInt(this._dice);
		buffer.writeInt(this._resultCellId);
		buffer.writeByte(this._resultCellType);
		buffer.writeInt(this._remainNormalDiceUseCount);
	}
}
