package net.sf.l2jdev.gameserver.network.serverpackets.training;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExTrainingZone_Leaving extends ServerPacket
{
	public static final ExTrainingZone_Leaving STATIC_PACKET = new ExTrainingZone_Leaving();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TRAININGZONE_LEAVING.writeId(this, buffer);
	}
}
