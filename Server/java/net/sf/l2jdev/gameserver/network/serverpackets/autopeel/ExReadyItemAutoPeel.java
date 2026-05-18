package net.sf.l2jdev.gameserver.network.serverpackets.autopeel;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExReadyItemAutoPeel extends ServerPacket
{
	private final boolean _result;
	private final int _itemObjectId;

	public ExReadyItemAutoPeel(boolean result, int itemObjectId)
	{
		this._result = result;
		this._itemObjectId = itemObjectId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_READY_ITEM_AUTO_PEEL.writeId(this, buffer);
		buffer.writeByte(this._result);
		buffer.writeInt(this._itemObjectId);
	}
}
