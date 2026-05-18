package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerSubChange implements IBaseEvent
{
	private final Player _player;

	public OnPlayerSubChange(Player player)
	{
		this._player = player;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_SUB_CHANGE;
	}
}
