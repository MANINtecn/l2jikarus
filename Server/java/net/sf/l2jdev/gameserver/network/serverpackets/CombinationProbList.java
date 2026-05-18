package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class CombinationProbList extends ServerPacket
{
	private final int _oneSlotServerId;
	private final int _twoSlotServerId;
	private final float _chance;

	public CombinationProbList(int oneSlotServerId, int twoSlotServerId, float chance)
	{
		this._oneSlotServerId = oneSlotServerId;
		this._twoSlotServerId = twoSlotServerId;
		this._chance = chance;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COMBINATION_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(this._oneSlotServerId);
		buffer.writeInt(this._twoSlotServerId);
		buffer.writeInt((int) (this._chance * 10000.0F));
	}
}
