package net.sf.l2jdev.gameserver.model.events.holders.instance;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;

public class OnInstanceCreated implements IBaseEvent
{
	private final Instance _instance;
	private final Player _creator;

	public OnInstanceCreated(Instance instance, Player creator)
	{
		this._instance = instance;
		this._creator = creator;
	}

	public Instance getInstanceWorld()
	{
		return this._instance;
	}

	public Player getCreator()
	{
		return this._creator;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_INSTANCE_CREATED;
	}
}
