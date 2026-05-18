package net.sf.l2jdev.gameserver.network.serverpackets.blackcoupon;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ItemRestoreResult extends ServerPacket
{
	public static final ItemRestoreResult FAIL = new ItemRestoreResult(1);
	public static final ItemRestoreResult SUCCESS = new ItemRestoreResult(0);
	private final int _type;

	public ItemRestoreResult(int type)
	{
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_RESTORE.writeId(this, buffer);
		buffer.writeByte(this._type);
	}
}
