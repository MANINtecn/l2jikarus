package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class WaterTask implements Runnable
{
	private final Player _player;

	public WaterTask(Player player)
	{
		this._player = player;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			double reduceHp = this._player.getMaxHp() / 100.0;
			if (reduceHp < 1.0)
			{
				reduceHp = 1.0;
			}

			this._player.reduceCurrentHp(reduceHp, this._player, null, false, true, false, false);
			this._player.sendMessage("You have taken " + reduceHp + " damage because you were unable to breathe.");
		}
	}
}
