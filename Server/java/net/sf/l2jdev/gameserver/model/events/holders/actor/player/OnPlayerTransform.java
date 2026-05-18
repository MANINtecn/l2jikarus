package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerTransform implements IBaseEvent
{
	private final Player _player;
	private final int _transformId;

	public OnPlayerTransform(Player player, int transformId)
	{
		this._player = player;
		this._transformId = transformId;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getTransformId()
	{
		return this._transformId;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_TRANSFORM;
	}
}
