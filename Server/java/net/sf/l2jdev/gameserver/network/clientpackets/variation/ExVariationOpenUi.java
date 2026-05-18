package net.sf.l2jdev.gameserver.network.clientpackets.variation;

import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExVariationOpenUi extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
	}
}
