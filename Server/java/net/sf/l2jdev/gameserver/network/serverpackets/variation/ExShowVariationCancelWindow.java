package net.sf.l2jdev.gameserver.network.serverpackets.variation;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowVariationCancelWindow extends ServerPacket
{
	public static final ExShowVariationCancelWindow STATIC_PACKET = new ExShowVariationCancelWindow();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_VARIATION_CANCEL_WINDOW.writeId(this, buffer);
	}
}
