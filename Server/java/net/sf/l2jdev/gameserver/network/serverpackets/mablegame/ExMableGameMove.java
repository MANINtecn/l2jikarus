package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGameMove extends ServerPacket
{
	private final int _moveDelta;
	private final int _resultCellId;
	private final int _resultCellType;

	public ExMableGameMove(int moveDelta, int resultCellId, int resultCellType)
	{
		this._moveDelta = moveDelta;
		this._resultCellId = resultCellId;
		this._resultCellType = resultCellType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_MOVE.writeId(this, buffer);
		buffer.writeInt(this._moveDelta);
		buffer.writeInt(this._resultCellId);
		buffer.writeByte(this._resultCellType);
	}
}
