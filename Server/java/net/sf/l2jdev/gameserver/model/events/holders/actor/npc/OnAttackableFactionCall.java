package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnAttackableFactionCall implements IBaseEvent
{
	private final Npc _npc;
	private final Npc _caller;
	private final Player _attacker;
	private final boolean _isSummon;

	public OnAttackableFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon)
	{
		this._npc = npc;
		this._caller = caller;
		this._attacker = attacker;
		this._isSummon = isSummon;
	}

	public Npc getNpc()
	{
		return this._npc;
	}

	public Npc getCaller()
	{
		return this._caller;
	}

	public Player getAttacker()
	{
		return this._attacker;
	}

	public boolean isSummon()
	{
		return this._isSummon;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ATTACKABLE_FACTION_CALL;
	}
}
