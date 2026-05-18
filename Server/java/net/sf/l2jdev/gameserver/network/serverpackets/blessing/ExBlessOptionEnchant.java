package net.sf.l2jdev.gameserver.network.serverpackets.blessing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBlessOptionEnchant extends ServerPacket
{
	private final int _scroll;
	private final int _result;

	public ExBlessOptionEnchant(int scroll, int result)
	{
		this._scroll = scroll;
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLESS_OPTION_ENCHANT.writeId(this, buffer);
		buffer.writeInt(this._scroll);
		buffer.writeInt(this._result);
	}
}
