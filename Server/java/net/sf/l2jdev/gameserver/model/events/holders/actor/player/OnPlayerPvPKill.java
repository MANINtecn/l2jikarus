package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerPvPKill implements IBaseEvent
{
	private final Player _player;
	private final Player _target;

	public OnPlayerPvPKill(Player player, Player target)
	{
		this._player = player;
		this._target = target;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Player getTarget()
	{
		return this._target;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_PVP_KILL;
	}
}
