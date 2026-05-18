package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeApplicantInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeWaitingUser extends ServerPacket
{
	private final PledgeApplicantInfo _pledgeRecruitInfo;

	public ExPledgeWaitingUser(PledgeApplicantInfo pledgeRecruitInfo)
	{
		this._pledgeRecruitInfo = pledgeRecruitInfo;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_WAITING_USER.writeId(this, buffer);
		buffer.writeInt(this._pledgeRecruitInfo.getPlayerId());
		buffer.writeString(this._pledgeRecruitInfo.getMessage());
	}
}
