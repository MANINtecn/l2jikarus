package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerBypass implements IBaseEvent
{
	private final Player _player;
	private final String _command;

	public OnPlayerBypass(Player player, String command)
	{
		this._player = player;
		this._command = command;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public String getCommand()
	{
		return this._command;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_BYPASS;
	}
}
