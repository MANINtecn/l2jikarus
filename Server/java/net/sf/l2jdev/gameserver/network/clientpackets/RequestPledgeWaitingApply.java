package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeApplicantInfo;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanEntryStatus;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeRecruitApplyInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeWaitingListAlarm;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPledgeWaitingApply extends ClientPacket
{
	private int _karma;
	private int _clanId;
	private String _message;

	@Override
	protected void readImpl()
	{
		this._karma = this.readInt();
		this._clanId = this.readInt();
		this._message = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getClan() == null)
		{
			Clan clan = ClanTable.getInstance().getClan(this._clanId);
			if (clan != null)
			{
				PledgeApplicantInfo info = new PledgeApplicantInfo(player.getObjectId(), player.getName(), player.getLevel(), this._karma, this._clanId, this._message);
				if (ClanEntryManager.getInstance().addPlayerApplicationToClan(this._clanId, info))
				{
					player.sendPacket(new ExPledgeRecruitApplyInfo(ClanEntryStatus.WAITING));
					Player clanLeader = World.getInstance().getPlayer(clan.getLeaderId());
					if (clanLeader != null)
					{
						clanLeader.sendPacket(ExPledgeWaitingListAlarm.STATIC_PACKET);
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MAY_APPLY_FOR_ENTRY_IN_S1_MIN_AFTER_CANCELLING_YOUR_APPLICATION);
					sm.addLong(ClanEntryManager.getInstance().getPlayerLockTime(player.getObjectId()));
					player.sendPacket(sm);
				}
			}
		}
	}
}
