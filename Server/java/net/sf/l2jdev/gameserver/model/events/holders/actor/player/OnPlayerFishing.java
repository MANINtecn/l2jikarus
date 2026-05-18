package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.fishing.FishingEndReason;

public class OnPlayerFishing implements IBaseEvent
{
	private final Player _player;
	private final FishingEndReason _reason;

	public OnPlayerFishing(Player player, FishingEndReason reason)
	{
		this._player = player;
		this._reason = reason;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public FishingEndReason getReason()
	{
		return this._reason;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_FISHING;
	}
}
