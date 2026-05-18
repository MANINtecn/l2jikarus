package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeApplicantInfo;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeRecruitInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeWaitingListApplied extends ServerPacket
{
	private final PledgeApplicantInfo _pledgePlayerRecruitInfo;
	private final PledgeRecruitInfo _pledgeRecruitInfo;

	public ExPledgeWaitingListApplied(int clanId, int playerId)
	{
		this._pledgePlayerRecruitInfo = ClanEntryManager.getInstance().getPlayerApplication(clanId, playerId);
		this._pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(clanId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_WAITING_LIST_APPLIED.writeId(this, buffer);
		Clan clan = this._pledgeRecruitInfo.getClan();
		buffer.writeInt(clan.getId());
		buffer.writeString(clan.getName());
		buffer.writeString(clan.getLeaderName());
		buffer.writeInt(clan.getLevel());
		buffer.writeInt(clan.getMembersCount());
		buffer.writeInt(this._pledgeRecruitInfo.getKarma());
		buffer.writeString(this._pledgeRecruitInfo.getInformation());
		buffer.writeString(this._pledgePlayerRecruitInfo.getMessage());
	}
}
