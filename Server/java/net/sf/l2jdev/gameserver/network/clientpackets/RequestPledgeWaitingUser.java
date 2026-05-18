package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeApplicantInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeWaitingList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeWaitingUser;

public class RequestPledgeWaitingUser extends ClientPacket
{
	private int _clanId;
	private int _playerId;

	@Override
	protected void readImpl()
	{
		this._clanId = this.readInt();
		this._playerId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getClanId() == this._clanId)
		{
			PledgeApplicantInfo infos = ClanEntryManager.getInstance().getPlayerApplication(this._clanId, this._playerId);
			if (infos == null)
			{
				player.sendPacket(new ExPledgeWaitingList(this._clanId));
			}
			else
			{
				player.sendPacket(new ExPledgeWaitingUser(infos));
			}
		}
	}
}
