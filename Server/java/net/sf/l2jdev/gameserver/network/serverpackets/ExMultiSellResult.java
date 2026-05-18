package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMultiSellResult extends ServerPacket
{
	private final int _success;
	private final int _type;
	private final int _count;

	public ExMultiSellResult(int success, int type, int count)
	{
		this._success = success;
		this._type = type;
		this._count = count;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MULTISELL_RESULT.writeId(this, buffer);
		buffer.writeByte(this._success);
		buffer.writeInt(this._type);
		buffer.writeInt(this._count);
	}
}
