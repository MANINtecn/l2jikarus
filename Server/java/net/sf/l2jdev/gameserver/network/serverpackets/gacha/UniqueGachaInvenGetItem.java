package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class UniqueGachaInvenGetItem extends ServerPacket
{
	private final int _result;

	public UniqueGachaInvenGetItem(boolean result)
	{
		this._result = result ? 1 : 0;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_INVEN_GET_ITEM.writeId(this, buffer);
		buffer.writeByte(this._result);
	}
}
