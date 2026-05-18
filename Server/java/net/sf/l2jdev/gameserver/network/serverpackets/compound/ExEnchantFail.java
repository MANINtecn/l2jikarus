package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantFail extends ServerPacket
{
	public static final ExEnchantFail STATIC_PACKET = new ExEnchantFail(0, 0);
	private final int _itemOne;
	private final int _itemTwo;

	public ExEnchantFail(int itemOne, int itemTwo)
	{
		this._itemOne = itemOne;
		this._itemTwo = itemTwo;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_FAIL.writeId(this, buffer);
		buffer.writeInt(this._itemOne);
		buffer.writeInt(this._itemTwo);
	}
}
