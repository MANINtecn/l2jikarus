package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class ResetChargesTask implements Runnable
{
	private final Player _player;

	public ResetChargesTask(Player player)
	{
		this._player = player;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			this._player.clearCharges();
		}
	}
}
