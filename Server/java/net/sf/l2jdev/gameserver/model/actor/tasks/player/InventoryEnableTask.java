package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class InventoryEnableTask implements Runnable
{
	private final Player _player;

	public InventoryEnableTask(Player player)
	{
		this._player = player;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			this._player.setInventoryBlockingStatus(false);
		}
	}
}
