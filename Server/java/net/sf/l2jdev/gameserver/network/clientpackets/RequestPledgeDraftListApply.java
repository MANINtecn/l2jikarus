package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeWaitingInfo;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPledgeDraftListApply extends ClientPacket
{
	private int _applyType;
	private int _karma;

	@Override
	protected void readImpl()
	{
		this._applyType = this.readInt();
		this._karma = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan != null && clan.getLeader().getObjectId() == player.getObjectId())
			{
				switch (this._applyType)
				{
					case 0:
						if (ClanEntryManager.getInstance().removeFromWaitingList(player.getObjectId()))
						{
							player.sendPacket(SystemMessageId.ENTRY_APPLICATION_CANCELLED_YOU_MAY_APPLY_TO_A_NEW_CLAN_AFTER_5_MIN);
						}
						break;
					case 1:
						PledgeWaitingInfo pledgeDraftList = new PledgeWaitingInfo(player.getObjectId(), player.getLevel(), this._karma, player.getPlayerClass().getId(), player.getName());
						if (ClanEntryManager.getInstance().addToWaitingList(player.getObjectId(), pledgeDraftList))
						{
							player.sendPacket(SystemMessageId.YOU_ARE_ADDED_TO_THE_WAITING_LIST_IF_YOU_DO_NOT_JOIN_A_CLAN_IN_30_D_YOU_WILL_BE_AUTOMATICALLY_DELETED_FROM_THE_LIST_IN_CASE_OF_LEAVING_THE_WAITING_LIST_YOU_WILL_NOT_BE_ABLE_TO_JOIN_IT_AGAIN_FOR_5_MIN);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MAY_APPLY_FOR_ENTRY_IN_S1_MIN_AFTER_CANCELLING_YOUR_APPLICATION);
							sm.addLong(ClanEntryManager.getInstance().getPlayerLockTime(player.getObjectId()));
							player.sendPacket(sm);
						}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
			}
		}
	}
}
