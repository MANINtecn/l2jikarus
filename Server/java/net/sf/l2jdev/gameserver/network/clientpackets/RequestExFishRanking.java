package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.network.PacketLogger;

public class RequestExFishRanking extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		PacketLogger.info("C5: RequestExFishRanking");
	}
}
