package net.sf.l2jdev.gameserver.model.cubic.conditions;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.Cubic;

public class HpCondition implements ICubicCondition
{
	private final HpCondition.HpConditionType _type;
	private final int _hpPer;

	public HpCondition(HpCondition.HpConditionType type, int hpPer)
	{
		this._type = type;
		this._hpPer = hpPer;
	}

	@Override
	public boolean test(Cubic cubic, Creature owner, WorldObject target)
	{
		if (target.isCreature() || target.isDoor())
		{
			double hpPer = (target.isDoor() ? target.asDoor() : target.asCreature()).getCurrentHpPercent();
			switch (this._type)
			{
				case GREATER:
					return hpPer > this._hpPer;
				case LESSER:
					return hpPer < this._hpPer;
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " chance: " + this._hpPer;
	}

	public static enum HpConditionType
	{
		GREATER,
		LESSER;
	}
}
