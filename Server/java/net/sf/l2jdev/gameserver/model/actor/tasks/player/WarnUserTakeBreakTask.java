package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class WarnUserTakeBreakTask implements Runnable
{
	private final Player _player;

	public WarnUserTakeBreakTask(Player player)
	{
		this._player = player;
	}

	@Override
	public void run()
	{
		if (this._player != null)
		{
			if (this._player.isOnline())
			{
				long hours = TimeUnit.MILLISECONDS.toHours(this._player.getUptime() + 60000L);
				this._player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_PLAYED_FOR_S1_H_TAKE_A_BREAK_PLEASE).addLong(hours));
			}
			else
			{
				this._player.stopWarnUserTakeBreak();
			}
		}
	}
}
