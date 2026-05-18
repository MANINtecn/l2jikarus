package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPartyRoomAnnounce;

public class RequestPartyMatchingAnnounce extends ClientPacket
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
			for (Player worldPlayers : World.getInstance().getPlayers())
			{
				if (!BlockList.isBlocked(worldPlayers, player))
				{
					worldPlayers.sendPacket(new ExPartyRoomAnnounce(player));
				}
			}
		}
	}
}
