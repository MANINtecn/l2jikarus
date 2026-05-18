package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerMoveRequest implements IBaseEvent
{
	private final Player _player;
	private final Location _location;

	public OnPlayerMoveRequest(Player player, Location loc)
	{
		this._player = player;
		this._location = loc;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Location getLocation()
	{
		return this._location;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_MOVE_REQUEST;
	}
}
