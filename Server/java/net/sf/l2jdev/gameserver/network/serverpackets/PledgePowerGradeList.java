package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgePowerGradeList extends ServerPacket
{
	private final Collection<Clan.RankPrivs> _privs;

	public PledgePowerGradeList(Collection<Clan.RankPrivs> privs)
	{
		this._privs = privs;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_POWER_GRADE_LIST.writeId(this, buffer);
		buffer.writeInt(this._privs.size());

		for (Clan.RankPrivs temp : this._privs)
		{
			buffer.writeInt(temp.getRank());
			buffer.writeInt(temp.getParty());
		}
	}
}
