package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerPressTutorialMark;

public class RequestTutorialQuestionMark extends ClientPacket
{
	private int _number = 0;

	@Override
	protected void readImpl()
	{
		this.readByte();
		this._number = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK, player))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPressTutorialMark(player, this._number), player);
			}
		}
	}
}
