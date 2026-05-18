package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnNpcCanBeSeen implements IBaseEvent
{
	private final Npc _npc;
	private final Player _player;

	public OnNpcCanBeSeen(Npc npc, Player player)
	{
		this._npc = npc;
		this._player = player;
	}

	public Npc getNpc()
	{
		return this._npc;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_CAN_BE_SEEN;
	}
}
