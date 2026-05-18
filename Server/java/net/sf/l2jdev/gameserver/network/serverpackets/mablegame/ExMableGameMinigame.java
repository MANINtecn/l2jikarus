package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGameMinigame extends ServerPacket
{
	private final int _bossType;
	private final int _luckyNumber;
	private final int _dice;
	private final int _bossDice;
	private final int _result;
	private final boolean _isLuckyNumber;
	private final int _rewardItemId;
	private final long _rewardItemCount;

	public ExMableGameMinigame(int bossType, int luckyNumber, int dice, int bossDice, int result, boolean isLuckyNumber, int rewardItemId, long rewardItemCount)
	{
		this._bossType = bossType;
		this._luckyNumber = luckyNumber;
		this._dice = dice;
		this._bossDice = bossDice;
		this._result = result;
		this._isLuckyNumber = isLuckyNumber;
		this._rewardItemId = rewardItemId;
		this._rewardItemCount = rewardItemCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_MINIGAME.writeId(this, buffer);
		buffer.writeInt(this._bossType);
		buffer.writeInt(this._luckyNumber);
		buffer.writeInt(this._dice);
		buffer.writeInt(this._bossDice);
		buffer.writeByte(this._result);
		buffer.writeByte(this._isLuckyNumber);
		buffer.writeInt(this._rewardItemId);
		buffer.writeLong(this._rewardItemCount);
	}
}
