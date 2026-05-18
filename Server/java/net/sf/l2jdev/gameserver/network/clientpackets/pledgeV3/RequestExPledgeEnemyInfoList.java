package net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeEnemyInfoList;

public class RequestExPledgeEnemyInfoList extends ClientPacket
{
	private int _playerClan;

	@Override
	protected void readImpl()
	{
		this._playerClan = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = ClanTable.getInstance().getClan(this._playerClan);
			if (clan != null && clan.getClanMember(player.getObjectId()) != null)
			{
				player.sendPacket(new ExPledgeEnemyInfoList(clan));
			}
		}
	}
}
