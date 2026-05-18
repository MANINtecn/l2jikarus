package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnAttackableKill implements IBaseEvent
{
	private final Player _attacker;
	private final Attackable _target;
	private final boolean _isSummon;

	public OnAttackableKill(Player attacker, Attackable target, boolean isSummon)
	{
		this._attacker = attacker;
		this._target = target;
		this._isSummon = isSummon;
	}

	public Player getAttacker()
	{
		return this._attacker;
	}

	public Attackable getTarget()
	{
		return this._target;
	}

	public boolean isSummon()
	{
		return this._isSummon;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ATTACKABLE_KILL;
	}
}
