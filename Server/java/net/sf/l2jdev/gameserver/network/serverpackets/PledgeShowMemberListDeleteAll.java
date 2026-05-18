package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeShowMemberListDeleteAll extends ServerPacket
{
	public static final PledgeShowMemberListDeleteAll STATIC_PACKET = new PledgeShowMemberListDeleteAll();

	private PledgeShowMemberListDeleteAll()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_DELETE_ALL.writeId(this, buffer);
	}
}
