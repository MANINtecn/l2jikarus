package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNotifyPremiumItem extends ServerPacket
{
	public static final ExNotifyPremiumItem STATIC_PACKET = new ExNotifyPremiumItem();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NOTIFY_PREMIUM_ITEM.writeId(this, buffer);
	}
}
