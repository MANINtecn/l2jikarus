package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanEntryStatus;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeRecruitApplyInfo extends ServerPacket
{
	private final ClanEntryStatus _status;

	public ExPledgeRecruitApplyInfo(ClanEntryStatus status)
	{
		this._status = status;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_RECRUIT_APPLY_INFO.writeId(this, buffer);
		buffer.writeInt(this._status.ordinal());
	}
}
