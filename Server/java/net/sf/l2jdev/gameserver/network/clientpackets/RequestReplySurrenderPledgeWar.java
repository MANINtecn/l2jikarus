package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;

public class RequestReplySurrenderPledgeWar extends ClientPacket
{
	private String _reqName;
	private int _answer;

	@Override
	protected void readImpl()
	{
		this._reqName = this.readString();
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
					PacketLogger.info(this.getClass().getSimpleName() + ": Missing implementation for answer: " + this._answer + " and name: " + this._reqName + "!");
				}

				player.onTransactionRequest(requestor);
			}
		}
	}
}
