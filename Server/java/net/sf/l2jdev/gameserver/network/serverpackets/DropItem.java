package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class DropItem extends ServerPacket
{
	private final Item _item;
	private final int _objectId;

	public DropItem(Item item, int playerObjId)
	{
		this._item = item;
		this._objectId = playerObjId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.DROP_ITEM.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._item.getObjectId());
		buffer.writeInt(this._item.getDisplayId());
		buffer.writeInt(this._item.getX());
		buffer.writeInt(this._item.getY());
		buffer.writeInt(this._item.getZ());
		buffer.writeByte(this._item.isStackable());
		buffer.writeLong(this._item.getCount());
		buffer.writeInt(0);
		buffer.writeByte(this._item.getEnchantLevel() > 0);
		buffer.writeInt(0);
		buffer.writeByte(this._item.getEnchantLevel());
		buffer.writeByte(this._item.getAugmentation() != null);
		buffer.writeByte(this._item.getSpecialAbilities().size());
		buffer.writeByte(0);
	}
}
