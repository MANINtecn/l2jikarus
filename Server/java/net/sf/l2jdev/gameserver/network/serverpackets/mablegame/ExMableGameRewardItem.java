package net.sf.l2jdev.gameserver.network.serverpackets.mablegame;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMableGameRewardItem extends ServerPacket
{
	private final int _itemId;
	private final long _itemCount;

	public ExMableGameRewardItem(int itemId, long itemCount)
	{
		this._itemId = itemId;
		this._itemCount = itemCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MABLE_GAME_REWARD_ITEM.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._itemCount);
	}
}
