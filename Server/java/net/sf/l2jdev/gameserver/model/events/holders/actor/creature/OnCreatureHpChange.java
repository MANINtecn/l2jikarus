package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnCreatureHpChange implements IBaseEvent
{
	private final Creature _creature;
	private final double _newHp;
	private final double _oldHp;

	public OnCreatureHpChange(Creature creature, double oldHp, double newHp)
	{
		this._creature = creature;
		this._oldHp = oldHp;
		this._newHp = newHp;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	public double getOldHp()
	{
		return this._oldHp;
	}

	public double getNewHp()
	{
		return this._newHp;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_HP_CHANGE;
	}
}
