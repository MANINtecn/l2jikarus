package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.network.serverpackets.ExBRVersion;

public class RequestBRVersion extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		this.getClient().sendPacket(new ExBRVersion());
	}
}
