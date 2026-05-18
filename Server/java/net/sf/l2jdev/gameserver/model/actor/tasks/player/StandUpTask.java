package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class StandUpTask implements Runnable
{
	private final Player _player;

	public StandUpTask(Player player)
	{
		this._player = player;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			this._player.setBlockActions(false);
			this._player.setSitting(false);
			this._player.setSittingProgress(false);
			this._player.getAI().setIntention(Intention.IDLE);
		}
	}
}
