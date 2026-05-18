package net.sf.l2jdev.gameserver.network.serverpackets.randomcraft;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCraftRandomMake extends ServerPacket
{
	private final int _itemId;
	private final long _itemCount;

	public ExCraftRandomMake(int itemId, long itemCount)
	{
		this._itemId = itemId;
		this._itemCount = itemCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CRAFT_RANDOM_MAKE.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeShort(15);
		buffer.writeInt(this._itemId);
		buffer.writeLong(this._itemCount);
		buffer.writeByte(0);
	}
}
