package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MovieHolder;

public class RequestExEscapeScene extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			MovieHolder holder = player.getMovieHolder();
			if (holder != null)
			{
				holder.playerEscapeVote(player);
			}
		}
	}
}
