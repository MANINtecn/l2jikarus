package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerSummonSpawn implements IBaseEvent
{
	private final Summon _summon;

	public OnPlayerSummonSpawn(Summon summon)
	{
		this._summon = summon;
	}

	public Summon getSummon()
	{
		return this._summon;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_SUMMON_SPAWN;
	}
}
