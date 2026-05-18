package net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeBonusMarkReset extends ServerPacket
{
	public static ExPledgeBonusMarkReset STATIC_PACKET = new ExPledgeBonusMarkReset();

	private ExPledgeBonusMarkReset()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_ACTIVITY_MARK_RESET.writeId(this, buffer);
	}
}
