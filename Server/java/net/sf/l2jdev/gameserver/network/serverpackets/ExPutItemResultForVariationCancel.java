package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPutItemResultForVariationCancel extends ServerPacket
{
	private final int _itemObjId;
	private final int _itemId;
	private final int _option1;
	private final int _option2;
	private final int _option3;
	private final long _price;

	public ExPutItemResultForVariationCancel(Item item, long price)
	{
		this._itemObjId = item.getObjectId();
		this._itemId = item.getDisplayId();
		this._price = price;
		VariationInstance augment = item.getAugmentation();
		this._option1 = augment.getOption1Id();
		this._option2 = augment.getOption2Id();
		this._option3 = augment.getOption3Id();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PUT_ITEM_RESULT_FOR_VARIATION_CANCEL.writeId(this, buffer);
		buffer.writeInt(this._itemObjId);
		buffer.writeInt(this._itemId);
		buffer.writeInt(this._option1);
		buffer.writeInt(this._option2);
		buffer.writeInt(this._option3);
		buffer.writeLong(this._price);
		buffer.writeInt(1);
	}
}
