package net.sf.l2jdev.gameserver.network.serverpackets.variation;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPutCommissionResultForVariationMake extends ServerPacket
{
	private final int _gemstoneObjId;
	private final int _itemId;
	private final long _gemstoneCount;
	private final int _unk1;
	private final int _unk2;

	public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count, int itemId)
	{
		this._gemstoneObjId = gemstoneObjId;
		this._itemId = itemId;
		this._gemstoneCount = count;
		this._unk1 = 0;
		this._unk2 = 1;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PUT_COMMISSION_RESULT_FOR_VARIATION_MAKE.writeId(this, buffer);
		buffer.writeInt(this._gemstoneObjId);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._gemstoneCount);
		buffer.writeLong(this._unk1);
		buffer.writeInt(this._unk2);
	}
}
