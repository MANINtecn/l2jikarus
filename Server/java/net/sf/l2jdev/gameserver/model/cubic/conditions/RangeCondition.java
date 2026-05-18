package net.sf.l2jdev.gameserver.model.cubic.conditions;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.Cubic;

public class RangeCondition implements ICubicCondition
{
	private final int _range;

	public RangeCondition(int range)
	{
		this._range = range;
	}

	@Override
	public boolean test(Cubic cubic, Creature owner, WorldObject target)
	{
		return owner.calculateDistance2D(target) <= this._range;
	}
}
