package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class TeleportTask implements Runnable
{
	private final Player _player;
	private final Location _loc;

	public TeleportTask(Player player, Location loc)
	{
		this._player = player;
		this._loc = loc;
	}

	@Override
	public void run()
	{
		if (this._player != null && this._player.isOnline())
		{
			this._player.teleToLocation(this._loc, true);
		}
	}
}
