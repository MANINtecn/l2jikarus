package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SpawnItem extends ServerPacket
{
	private final Item _item;

	public SpawnItem(Item item)
	{
		this._item = item;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SPAWN_ITEM.writeId(this, buffer);
		buffer.writeInt(this._item.getObjectId());
		buffer.writeInt(this._item.getDisplayId());
		buffer.writeInt(this._item.getX());
		buffer.writeInt(this._item.getY());
		buffer.writeInt(this._item.getZ());
		buffer.writeInt(this._item.isStackable());
		buffer.writeLong(this._item.getCount());
		buffer.writeInt(0);
		buffer.writeByte(this._item.getEnchantLevel());
		buffer.writeByte(this._item.getAugmentation() != null);
		buffer.writeByte(this._item.getSpecialAbilities().size());
		buffer.writeByte(0);
	}
}
