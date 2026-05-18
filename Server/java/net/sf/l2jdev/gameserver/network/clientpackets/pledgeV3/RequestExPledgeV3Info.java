package net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeReceiveWarList;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeClassicRaidInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeV3Info;

public class RequestExPledgeV3Info extends ClientPacket
{
	private int _page;

	@Override
	protected void readImpl()
	{
		this._page = this.readByte();
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
				player.sendPacket(new ExPledgeV3Info(clan.getExp(), clan.getRank(), clan.getNotice(), clan.isNoticeEnabled()));
				player.sendPacket(new PledgeReceiveWarList(clan, this._page));
				player.sendPacket(new ExPledgeClassicRaidInfo(player));
			}
		}
	}
}
