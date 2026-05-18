package net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeDonationInfo extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_DONATION_INFO.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeByte(1);
	}
}
