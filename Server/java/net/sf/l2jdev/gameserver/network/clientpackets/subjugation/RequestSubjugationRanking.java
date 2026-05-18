package net.sf.l2jdev.gameserver.network.clientpackets.subjugation;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.subjugation.ExSubjugationRanking;

public class RequestSubjugationRanking extends ClientPacket
{
	private int _rankingCategory;

	@Override
	protected void readImpl()
	{
		this._rankingCategory = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExSubjugationRanking(this._rankingCategory, player.getObjectId()));
		}
	}
}
