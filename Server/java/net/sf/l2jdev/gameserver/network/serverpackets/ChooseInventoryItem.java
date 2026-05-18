package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ChooseInventoryItem extends ServerPacket
{
	private final int _itemId;

	public ChooseInventoryItem(int itemId)
	{
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHOOSE_INVENTORY_ITEM.writeId(this, buffer);
		buffer.writeInt(this._itemId);
	}
}
