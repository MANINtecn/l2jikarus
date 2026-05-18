package net.sf.l2jdev.gameserver.network.serverpackets.variation;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowVariationMakeWindow extends ServerPacket
{
	public static final ExShowVariationMakeWindow STATIC_PACKET = new ExShowVariationMakeWindow();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_VARIATION_MAKE_WINDOW.writeId(this, buffer);
	}
}
