package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public class RequestPledgeMemberPowerInfo extends ClientPacket
{
	protected int _unk1;
	private String _player;

	@Override
	protected void readImpl()
	{
		this._unk1 = this.readInt();
		this._player = this.readString();
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
				ClanMember member = clan.getClanMember(this._player);
				if (member != null)
				{
					player.sendPacket(new PledgeReceivePowerInfo(member));
				}
			}
		}
	}
}
