package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class StopMoveToward extends ClientPacket
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
			player.broadcastPacket(new net.sf.l2jdev.gameserver.network.serverpackets.MoveToLocation(player));
			player.stopMove(player.getLocation());
		}
	}
}
