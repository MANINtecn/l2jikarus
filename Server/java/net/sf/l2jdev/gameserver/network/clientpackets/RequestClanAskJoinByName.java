package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.serverpackets.AskJoinPledge;

public class RequestClanAskJoinByName extends ClientPacket
{
	private String _playerName;
	private int _pledgeType;

	@Override
	protected void readImpl()
	{
		this._playerName = this.readString();
		this._pledgeType = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan != null)
			{
				Player invitedPlayer = World.getInstance().getPlayer(this._playerName);
				if (clan.checkClanJoinCondition(player, invitedPlayer, this._pledgeType))
				{
					if (player.getRequest().setRequest(invitedPlayer, this))
					{
						invitedPlayer.sendPacket(new AskJoinPledge(player, clan.getName()));
					}
				}
			}
		}
	}

	public int getPledgeType()
	{
		return this._pledgeType;
	}
}
