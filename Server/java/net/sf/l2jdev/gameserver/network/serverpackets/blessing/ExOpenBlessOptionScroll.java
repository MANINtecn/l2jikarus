package net.sf.l2jdev.gameserver.network.serverpackets.blessing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOpenBlessOptionScroll extends ServerPacket
{
	private final int _itemId;

	public ExOpenBlessOptionScroll(int itemId)
	{
		this._itemId = itemId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OPEN_BLESS_OPTION_SCROLL.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeInt((int) (RatesConfig.BLESSING_CHANCE * 100000.0));
	}
}
