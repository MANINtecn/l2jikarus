package net.sf.l2jdev.gameserver.model.events.holders.sieges;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.siege.FortSiege;

public class OnFortSiegeStart implements IBaseEvent
{
	private final FortSiege _siege;

	public OnFortSiegeStart(FortSiege siege)
	{
		this._siege = siege;
	}

	public FortSiege getSiege()
	{
		return this._siege;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_FORT_SIEGE_START;
	}
}
