package net.sf.l2jdev.gameserver.model.events.holders.instance;

import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;

public class OnInstanceStatusChange implements IBaseEvent
{
	private final Instance _world;
	private final int _status;

	public OnInstanceStatusChange(Instance world, int status)
	{
		this._world = world;
		this._status = status;
	}

	public Instance getWorld()
	{
		return this._world;
	}

	public int getStatus()
	{
		return this._status;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_INSTANCE_STATUS_CHANGE;
	}
}
