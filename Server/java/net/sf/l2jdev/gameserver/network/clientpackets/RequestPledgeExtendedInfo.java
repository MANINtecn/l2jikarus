package net.sf.l2jdev.gameserver.network.clientpackets;

public class RequestPledgeExtendedInfo extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readString();
	}

	@Override
	protected void runImpl()
	{
	}
}
