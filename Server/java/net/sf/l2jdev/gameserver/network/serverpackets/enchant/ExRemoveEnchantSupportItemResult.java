package net.sf.l2jdev.gameserver.network.serverpackets.enchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRemoveEnchantSupportItemResult extends ServerPacket
{
	public static final ExRemoveEnchantSupportItemResult STATIC_PACKET = new ExRemoveEnchantSupportItemResult();

	private ExRemoveEnchantSupportItemResult()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REMOVE_ENCHANT_SUPPORT_ITEM_RESULT.writeId(this, buffer);
	}
}
