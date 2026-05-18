package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.network.serverpackets.PvpBookList;

public class ExPvpBookList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		this.getClient().sendPacket(new PvpBookList());
	}
}
