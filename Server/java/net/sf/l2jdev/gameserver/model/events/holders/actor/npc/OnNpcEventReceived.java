package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnNpcEventReceived implements IBaseEvent
{
	private final String _eventName;
	private final Npc _sender;
	private final Npc _receiver;
	private final WorldObject _reference;

	public OnNpcEventReceived(String eventName, Npc sender, Npc receiver, WorldObject reference)
	{
		this._eventName = eventName;
		this._sender = sender;
		this._receiver = receiver;
		this._reference = reference;
	}

	public String getEventName()
	{
		return this._eventName;
	}

	public Npc getSender()
	{
		return this._sender;
	}

	public Npc getReceiver()
	{
		return this._receiver;
	}

	public WorldObject getReference()
	{
		return this._reference;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_EVENT_RECEIVED;
	}
}
