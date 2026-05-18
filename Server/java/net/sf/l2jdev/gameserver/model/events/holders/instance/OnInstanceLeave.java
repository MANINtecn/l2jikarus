package net.sf.l2jdev.gameserver.model.events.holders.instance;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;

public class OnInstanceLeave implements IBaseEvent
{
	private final Player _player;
	private final Instance _instance;

	public OnInstanceLeave(Player player, Instance instance)
	{
		this._player = player;
		this._instance = instance;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Instance getInstanceWorld()
	{
		return this._instance;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_INSTANCE_LEAVE;
	}
}
