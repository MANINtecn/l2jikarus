package net.sf.l2jdev.gameserver.network.serverpackets.enchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPutEnchantTargetItemResult extends ServerPacket
{
	private final int _result;

	public ExPutEnchantTargetItemResult(int result)
	{
		this._result = result;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PUT_ENCHANT_TARGET_ITEM_RESULT.writeId(this, buffer);
		buffer.writeInt(this._result);
	}
}
