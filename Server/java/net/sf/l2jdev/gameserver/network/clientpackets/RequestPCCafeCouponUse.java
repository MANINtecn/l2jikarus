package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.network.PacketLogger;

public class RequestPCCafeCouponUse extends ClientPacket
{
	private String _str;

	@Override
	protected void readImpl()
	{
		this._str = this.readString();
	}

	@Override
	protected void runImpl()
	{
		PacketLogger.info("C5: RequestPCCafeCouponUse: S: " + this._str);
	}
}
