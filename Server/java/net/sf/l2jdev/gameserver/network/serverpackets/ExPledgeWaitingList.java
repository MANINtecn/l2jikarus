package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeApplicantInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeWaitingList extends ServerPacket
{
	private final Map<Integer, PledgeApplicantInfo> _pledgePlayerRecruitInfos;

	public ExPledgeWaitingList(int clanId)
	{
		this._pledgePlayerRecruitInfos = ClanEntryManager.getInstance().getApplicantListForClan(clanId);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_WAITING_LIST.writeId(this, buffer);
		buffer.writeInt(this._pledgePlayerRecruitInfos.size());

		for (PledgeApplicantInfo recruitInfo : this._pledgePlayerRecruitInfos.values())
		{
			buffer.writeInt(recruitInfo.getPlayerId());
			buffer.writeString(recruitInfo.getPlayerName());
			buffer.writeInt(recruitInfo.getClassId());
			buffer.writeInt(recruitInfo.getPlayerLvl());
		}
	}
}
