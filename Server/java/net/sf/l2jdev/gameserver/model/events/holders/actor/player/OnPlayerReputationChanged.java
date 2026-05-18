package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerReputationChanged implements IBaseEvent
{
	private final Player _player;
	private final int _oldReputation;
	private final int _newReputation;

	public OnPlayerReputationChanged(Player player, int oldReputation, int newReputation)
	{
		this._player = player;
		this._oldReputation = oldReputation;
		this._newReputation = newReputation;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getOldReputation()
	{
		return this._oldReputation;
	}

	public int getNewReputation()
	{
		return this._newReputation;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_REPUTATION_CHANGED;
	}
}
