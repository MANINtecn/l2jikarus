package net.sf.l2jdev.gameserver.model.events.holders;

import net.sf.l2jdev.gameserver.model.events.EventType;

public class OnDayNightChange implements IBaseEvent
{
	private final boolean _isNight;

	public OnDayNightChange(boolean isNight)
	{
		this._isNight = isNight;
	}

	public boolean isNight()
	{
		return this._isNight;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_DAY_NIGHT_CHANGE;
	}
}
