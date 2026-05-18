package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeCount extends ServerPacket
{
	private final int _count;

	public ExPledgeCount(Clan clan)
	{
		this._count = clan.getOnlineMembersCount();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_COUNT.writeId(this, buffer);
		buffer.writeInt(this._count);
	}
}
