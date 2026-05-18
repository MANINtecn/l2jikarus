package net.sf.l2jdev.gameserver.network.clientpackets.ranking;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExPetRankingMyInfo;

public class RequestPetRankingMyInfo extends ClientPacket
{
	private int _petId;

	@Override
	protected void readImpl()
	{
		this._petId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExPetRankingMyInfo(player, this._petId));
		}
	}
}
