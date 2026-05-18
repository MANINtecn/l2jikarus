package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class OnCreatureZoneExit implements IBaseEvent
{
	private final Creature _creature;
	private final ZoneType _zone;

	public OnCreatureZoneExit(Creature creature, ZoneType zone)
	{
		this._creature = creature;
		this._zone = zone;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	public ZoneType getZone()
	{
		return this._zone;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_ZONE_EXIT;
	}
}
