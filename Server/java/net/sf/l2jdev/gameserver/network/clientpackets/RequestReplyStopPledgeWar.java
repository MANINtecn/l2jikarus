package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestReplyStopPledgeWar extends ClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		this.readString();
		this._answer = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player requestor = player.getActiveRequester();
			if (requestor != null)
			{
				if (this._answer == 1)
				{
					ClanTable.getInstance().deleteClanWars(requestor.getClanId(), player.getClanId());
				}
				else
				{
					requestor.sendPacket(SystemMessageId.REQUEST_TO_END_WAR_HAS_BEEN_DENIED);
				}

				player.setActiveRequester(null);
				requestor.onTransactionResponse();
			}
		}
	}
}
