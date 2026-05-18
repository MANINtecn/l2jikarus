package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeRecruitInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeRecruitBoardDetail;

public class RequestPledgeRecruitBoardDetail extends ClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		this._clanId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PledgeRecruitInfo pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(this._clanId);
			if (pledgeRecruitInfo != null)
			{
				player.sendPacket(new ExPledgeRecruitBoardDetail(pledgeRecruitInfo));
			}
		}
	}
}
