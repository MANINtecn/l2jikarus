package net.sf.l2jdev.gameserver.network.serverpackets.compound;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnchantSucess extends ServerPacket
{
	private final int _itemId;
	private final int _enchantLevel;

	public ExEnchantSucess(int itemId, int enchantLevel)
	{
		this._itemId = itemId;
		this._enchantLevel = enchantLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_SUCCESS.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeInt(this._enchantLevel);
	}
}
