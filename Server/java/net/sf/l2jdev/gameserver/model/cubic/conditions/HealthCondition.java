package net.sf.l2jdev.gameserver.model.cubic.conditions;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.Cubic;

public class HealthCondition implements ICubicCondition
{
	private final int _min;
	private final int _max;

	public HealthCondition(int min, int max)
	{
		this._min = min;
		this._max = max;
	}

	@Override
	public boolean test(Cubic cubic, Creature owner, WorldObject target)
	{
		if (!target.isCreature() && !target.isDoor())
		{
			return false;
		}
		double hpPer = (target.isDoor() ? target.asDoor() : target.asCreature()).getCurrentHpPercent();
		return hpPer > this._min && hpPer < this._max;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " min: " + this._min + " max: " + this._max;
	}
}
