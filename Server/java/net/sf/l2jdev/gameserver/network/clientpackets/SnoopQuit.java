package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class SnoopQuit extends ClientPacket
{
	private int _snoopID;

	@Override
	protected void readImpl()
	{
		this._snoopID = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player target = World.getInstance().getPlayer(this._snoopID);
		if (target != null)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				target.removeSnooper(player);
				player.removeSnooped(target);
			}
		}
	}
}
