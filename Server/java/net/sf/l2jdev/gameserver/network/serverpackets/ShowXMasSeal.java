package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShowXMasSeal extends ServerPacket
{
	private final int _item;

	public ShowXMasSeal(int item)
	{
		this._item = item;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHOW_XMAS_SEAL.writeId(this, buffer);
		buffer.writeInt(this._item);
	}
}
