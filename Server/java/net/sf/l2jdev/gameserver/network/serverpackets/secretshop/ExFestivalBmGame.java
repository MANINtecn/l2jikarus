package net.sf.l2jdev.gameserver.network.serverpackets.secretshop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExFestivalBmGame extends ServerPacket
{
	private final int _ticketId;
	private final int _amount;
	private final int _ticketPerGame;
	private final int _rewardGrade;
	private final int _rewardId;
	private final int _rewardCount;
	private final int _result;

	public ExFestivalBmGame(int ticketId, int amount, int ticketPerGame, int rewardGrade, int rewardId, int rewardCount, int result)
	{
		this._ticketId = ticketId;
		this._amount = amount;
		this._ticketPerGame = ticketPerGame;
		this._rewardGrade = rewardGrade;
		this._rewardId = rewardId;
		this._rewardCount = rewardCount;
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FESTIVAL_BM_GAME.writeId(this, buffer);
		buffer.writeByte(this._result);
		buffer.writeInt(this._ticketId);
		buffer.writeLong(this._amount);
		buffer.writeInt(this._ticketPerGame);
		buffer.writeByte(this._rewardGrade);
		buffer.writeInt(this._rewardId);
		buffer.writeInt(this._rewardCount);
	}
}
