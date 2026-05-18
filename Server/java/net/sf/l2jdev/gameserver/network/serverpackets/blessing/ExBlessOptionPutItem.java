package net.sf.l2jdev.gameserver.network.serverpackets.blessing;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBlessOptionPutItem extends ServerPacket
{
	private final int _result;

	public ExBlessOptionPutItem(int result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLESS_OPTION_PUT_ITEM.writeId(this, buffer);
		buffer.writeByte(this._result);
	}
}
