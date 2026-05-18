package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShowPCCafeCouponShowUI extends ServerPacket
{
	public static final ShowPCCafeCouponShowUI STATIC_PACKET = new ShowPCCafeCouponShowUI();

	private ShowPCCafeCouponShowUI()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PCCAFE_COUPON_SHOW_UI.writeId(this, buffer);
	}
}
