package net.sf.l2jdev.gameserver.model.events.holders.sieges;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.siege.Siege;

public class OnCastleSiegeStart implements IBaseEvent
{
	private final Siege _siege;

	public OnCastleSiegeStart(Siege siege)
	{
		this._siege = siege;
	}

	public Siege getSiege()
	{
		return this._siege;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CASTLE_SIEGE_START;
	}
}
