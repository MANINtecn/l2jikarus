package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerPvPChanged implements IBaseEvent
{
	private final Player _player;
	private final int _oldPoints;
	private final int _newPoints;

	public OnPlayerPvPChanged(Player player, int oldPoints, int newPoints)
	{
		this._player = player;
		this._oldPoints = oldPoints;
		this._newPoints = newPoints;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getOldPoints()
	{
		return this._oldPoints;
	}

	public int getNewPoints()
	{
		return this._newPoints;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_PVP_CHANGED;
	}
}
