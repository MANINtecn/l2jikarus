package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerFameChanged implements IBaseEvent
{
	private final Player _player;
	private final int _oldFame;
	private final int _newFame;

	public OnPlayerFameChanged(Player player, int oldFame, int newFame)
	{
		this._player = player;
		this._oldFame = oldFame;
		this._newFame = newFame;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getOldFame()
	{
		return this._oldFame;
	}

	public int getNewFame()
	{
		return this._newFame;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_FAME_CHANGED;
	}
}
