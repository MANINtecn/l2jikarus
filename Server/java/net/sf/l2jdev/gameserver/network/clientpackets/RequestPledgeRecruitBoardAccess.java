package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeRecruitInfo;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPledgeRecruitBoardAccess extends ClientPacket
{
	private int _applyType;
	private int _karma;
	private String _information;
	private String _datailedInformation;
	private int _applicationType;
	private int _recruitingType;

	@Override
	protected void readImpl()
	{
		this._applyType = this.readInt();
		this._karma = this.readInt();
		this._information = this.readString();
		this._datailedInformation = this.readString();
		this._applicationType = this.readInt();
		this._recruitingType = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan == null)
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
			}
			else if (!player.hasAccess(ClanAccess.MODIFY_RANKS))
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
			}
			else
			{
				PledgeRecruitInfo pledgeRecruitInfo = new PledgeRecruitInfo(clan.getId(), this._karma, this._information, this._datailedInformation, this._applicationType, this._recruitingType);
				switch (this._applyType)
				{
					case 0:
						ClanEntryManager.getInstance().removeFromClanList(clan.getId());
						break;
					case 1:
						if (ClanEntryManager.getInstance().addToClanList(clan.getId(), pledgeRecruitInfo))
						{
							player.sendPacket(SystemMessageId.ENTRY_APPLICATION_COMPLETE_USE_MY_APPLICATION_TO_CHECK_OR_CANCEL_YOUR_APPLICATION_APPLICATION_IS_AUTOMATICALLY_CANCELLED_AFTER_30_D_IF_YOU_CANCEL_APPLICATION_YOU_CANNOT_APPLY_AGAIN_FOR_5_MIN);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MAY_APPLY_FOR_ENTRY_IN_S1_MIN_AFTER_CANCELLING_YOUR_APPLICATION);
							sm.addLong(ClanEntryManager.getInstance().getClanLockTime(clan.getId()));
							player.sendPacket(sm);
						}
						break;
					case 2:
						if (ClanEntryManager.getInstance().updateClanList(clan.getId(), pledgeRecruitInfo))
						{
							player.sendPacket(SystemMessageId.ENTRY_APPLICATION_COMPLETE_USE_MY_APPLICATION_TO_CHECK_OR_CANCEL_YOUR_APPLICATION_APPLICATION_IS_AUTOMATICALLY_CANCELLED_AFTER_30_D_IF_YOU_CANCEL_APPLICATION_YOU_CANNOT_APPLY_AGAIN_FOR_5_MIN);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MAY_APPLY_FOR_ENTRY_IN_S1_MIN_AFTER_CANCELLING_YOUR_APPLICATION);
							sm.addLong(ClanEntryManager.getInstance().getClanLockTime(clan.getId()));
							player.sendPacket(sm);
						}
				}
			}
		}
	}
}
