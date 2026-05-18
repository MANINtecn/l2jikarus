package net.sf.l2jdev.gameserver.network.serverpackets.blessing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBlessOptionProbList extends ServerPacket
{
	private final int _scrollId;
	private final int _itemId;

	public ExBlessOptionProbList(int scrollId, int itemId)
	{
		this._scrollId = scrollId;
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLESS_OPTION_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(this._scrollId);
		buffer.writeInt(this._itemId);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
