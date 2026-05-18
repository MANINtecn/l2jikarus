package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeRecruitInfo;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPledgeRecruitBoardSearch extends ServerPacket
{
	final List<PledgeRecruitInfo> _clanList;
	private final int _currentPage;
	private final int _totalNumberOfPage;
	private final int _clanOnCurrentPage;
	private final int _startIndex;
	private final int _endIndex;
	static final int CLAN_PER_PAGE = 12;

	public ExPledgeRecruitBoardSearch(List<PledgeRecruitInfo> clanList, int currentPage)
	{
		this._clanList = clanList;
		this._currentPage = currentPage;
		this._totalNumberOfPage = (int) Math.ceil(this._clanList.size() / 12.0);
		this._startIndex = Math.max(0, (this._currentPage - 1) * 12);
		this._endIndex = Math.min(this._startIndex + 12, this._clanList.size());
		this._clanOnCurrentPage = this._endIndex - this._startIndex;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_RECRUIT_BOARD_SEARCH.writeId(this, buffer);
		buffer.writeInt(this._currentPage);
		buffer.writeInt(this._totalNumberOfPage);
		buffer.writeInt(this._clanOnCurrentPage);

		for (int i = this._startIndex; i < this._endIndex; i++)
		{
			PledgeRecruitInfo recruitInfo = this._clanList.get(i);
			buffer.writeInt(recruitInfo.getClanId());
			buffer.writeInt(recruitInfo.getClan().getAllyId());
		}

		for (int i = this._startIndex; i < this._endIndex; i++)
		{
			PledgeRecruitInfo recruitInfo = this._clanList.get(i);
			Clan clan = recruitInfo.getClan();
			buffer.writeInt(clan.getCrestId());
			buffer.writeInt(clan.getAllyCrestId());
			buffer.writeString(clan.getName());
			buffer.writeString(clan.getLeaderName());
			buffer.writeInt(clan.getLevel());
			buffer.writeInt(clan.getMembersCount());
			buffer.writeInt(recruitInfo.getKarma());
			buffer.writeString(recruitInfo.getInformation());
			buffer.writeInt(recruitInfo.getApplicationType());
			buffer.writeInt(recruitInfo.getRecruitType());
		}
	}
}
