package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class HennaDurationTask implements Runnable
{
	private final Player _player;
	private final int _slot;

	public HennaDurationTask(Player player, int slot)
	{
		this._player = player;
		this._slot = slot;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			this._player.removeHenna(this._slot);
		}
	}
}
