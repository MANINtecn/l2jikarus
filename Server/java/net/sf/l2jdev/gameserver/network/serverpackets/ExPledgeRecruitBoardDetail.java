package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeRecruitInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeRecruitBoardDetail extends ServerPacket
{
	final PledgeRecruitInfo _pledgeRecruitInfo;

	public ExPledgeRecruitBoardDetail(PledgeRecruitInfo pledgeRecruitInfo)
	{
		this._pledgeRecruitInfo = pledgeRecruitInfo;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_RECRUIT_BOARD_DETAIL.writeId(this, buffer);
		buffer.writeInt(this._pledgeRecruitInfo.getClanId());
		buffer.writeInt(this._pledgeRecruitInfo.getKarma());
		buffer.writeString(this._pledgeRecruitInfo.getInformation());
		buffer.writeString(this._pledgeRecruitInfo.getDetailedInformation());
		buffer.writeInt(this._pledgeRecruitInfo.getApplicationType());
		buffer.writeInt(this._pledgeRecruitInfo.getRecruitType());
	}
}
