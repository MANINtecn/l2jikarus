package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeReceiveWarList;

public class RequestPledgeWarList extends ClientPacket
{
	protected int _page;
	private int _tab;

	@Override
	protected void readImpl()
	{
		this._page = this.readInt();
		this._tab = this.readInt();
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
				player.sendPacket(new PledgeReceiveWarList(clan, this._tab));
			}
		}
	}
}
