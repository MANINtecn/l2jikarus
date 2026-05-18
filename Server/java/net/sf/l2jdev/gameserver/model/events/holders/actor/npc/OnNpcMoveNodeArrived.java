package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnNpcMoveNodeArrived implements IBaseEvent
{
	private final Npc _npc;

	public OnNpcMoveNodeArrived(Npc npc)
	{
		this._npc = npc;
	}

	public Npc getNpc()
	{
		return this._npc;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_MOVE_NODE_ARRIVED;
	}
}
