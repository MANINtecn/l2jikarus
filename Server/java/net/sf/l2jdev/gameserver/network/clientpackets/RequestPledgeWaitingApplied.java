package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeWaitingListApplied;

public class RequestPledgeWaitingApplied extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getClan() == null)
		{
			int clanId = ClanEntryManager.getInstance().getClanIdForPlayerApplication(player.getObjectId());
			if (clanId > 0)
			{
				player.sendPacket(new ExPledgeWaitingListApplied(clanId, player.getObjectId()));
			}
		}
	}
}
