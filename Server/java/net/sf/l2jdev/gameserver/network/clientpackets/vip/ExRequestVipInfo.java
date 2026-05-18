package net.sf.l2jdev.gameserver.network.clientpackets.vip;

import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.vip.ReceiveVipInfo;

public class ExRequestVipInfo extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		this.getClient().sendPacket(new ReceiveVipInfo(this.getPlayer()));
	}
}
