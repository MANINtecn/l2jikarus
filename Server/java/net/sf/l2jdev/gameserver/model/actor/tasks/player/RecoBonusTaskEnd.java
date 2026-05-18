package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVoteSystemInfo;

public class RecoBonusTaskEnd implements Runnable
{
	private final Player _player;

	public RecoBonusTaskEnd(Player player)
	{
		this._player = player;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			this._player.sendPacket(new ExVoteSystemInfo(this._player));
		}
	}
}
