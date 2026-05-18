package net.sf.l2jdev.gameserver.network.serverpackets.attributechange;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExChangeAttributeItemList extends AbstractItemPacket
{
	private final int _type;
	private final int _itemId;
	private final List<ItemInfo> _itemsList;

	public ExChangeAttributeItemList(int type, int itemId, List<ItemInfo> itemList)
	{
		this._type = type;
		this._itemId = itemId;
		this._itemsList = itemList;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHANGE_ATTRIBUTE_ITEM_LIST.writeId(this, buffer);
		buffer.writeByte(this._type);
		buffer.writeInt(this._itemId);
		buffer.writeInt(this._itemsList.size());

		for (ItemInfo item : this._itemsList)
		{
			this.writeItem(item, buffer);
		}
	}
}
