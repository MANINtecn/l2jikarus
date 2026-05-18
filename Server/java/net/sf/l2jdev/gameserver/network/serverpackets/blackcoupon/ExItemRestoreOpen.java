package net.sf.l2jdev.gameserver.network.serverpackets.blackcoupon;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExItemRestoreOpen extends ServerPacket
{
	private final int _itemId;

	public ExItemRestoreOpen(int itemId)
	{
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_RESTORE_OPEN.writeId(this, buffer);
		buffer.writeInt(this._itemId);
	}
}
