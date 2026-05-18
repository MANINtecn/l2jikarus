package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionRegister extends ServerPacket
{
	private final int _success;
	private final int _collectionId;
	private final int _index;
	private final ItemEnchantHolder _collectionInfo;

	public ExCollectionRegister(boolean success, int collectionId, int index, ItemEnchantHolder collectionInfo)
	{
		this._success = success ? 1 : 0;
		this._collectionId = collectionId;
		this._index = index;
		this._collectionInfo = collectionInfo;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_REGISTER.writeId(this, buffer);
		buffer.writeShort(this._collectionId);
		buffer.writeByte(this._success);
		buffer.writeByte(0);
		buffer.writeShort(249);
		buffer.writeByte(this._index);
		buffer.writeInt(this._collectionInfo.getId());
		buffer.writeShort(this._collectionInfo.getEnchantLevel());
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeInt((int) this._collectionInfo.getCount());
	}
}
