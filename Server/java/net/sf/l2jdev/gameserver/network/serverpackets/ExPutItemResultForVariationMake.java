package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPutItemResultForVariationMake extends ServerPacket
{
	private final int _itemObjId;
	private final int _itemId;

	public ExPutItemResultForVariationMake(int itemObjId, int itemId)
	{
		this._itemObjId = itemObjId;
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PUT_ITEM_RESULT_FOR_VARIATION_MAKE.writeId(this, buffer);
		buffer.writeInt(this._itemObjId);
		buffer.writeInt(this._itemId);
		buffer.writeInt(1);
	}
}
