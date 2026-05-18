package net.sf.l2jdev.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.Cubic;
import net.sf.l2jdev.gameserver.model.cubic.CubicSkill;
import net.sf.l2jdev.gameserver.model.cubic.CubicTargetType;
import net.sf.l2jdev.gameserver.model.cubic.ICubicConditionHolder;
import net.sf.l2jdev.gameserver.model.cubic.conditions.ICubicCondition;

public class CubicTemplate extends CreatureTemplate implements ICubicConditionHolder
{
	private final int _id;
	private final int _level;
	private final int _slot;
	private final int _duration;
	private final int _delay;
	private final int _maxCount;
	private final int _useUp;
	private final double _power;
	private final CubicTargetType _targetType;
	private final List<ICubicCondition> _conditions = new ArrayList<>();
	private final List<CubicSkill> _skills = new ArrayList<>();

	public CubicTemplate(StatSet set)
	{
		super(set);
		this._id = set.getInt("id");
		this._level = set.getInt("level");
		this._slot = set.getInt("slot");
		this._duration = set.getInt("duration");
		this._delay = set.getInt("delay");
		this._maxCount = set.getInt("maxCount");
		this._useUp = set.getInt("useUp");
		this._power = set.getDouble("power") / 10.0;
		this._targetType = set.getEnum("targetType", CubicTargetType.class, CubicTargetType.TARGET);
	}

	public int getId()
	{
		return this._id;
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getSlot()
	{
		return this._slot;
	}

	public int getDuration()
	{
		return this._duration;
	}

	public int getDelay()
	{
		return this._delay;
	}

	public int getMaxCount()
	{
		return this._maxCount;
	}

	public int getUseUp()
	{
		return this._useUp;
	}

	public CubicTargetType getTargetType()
	{
		return this._targetType;
	}

	public List<CubicSkill> getCubicSkills()
	{
		return this._skills;
	}

	@Override
	public int getBasePAtk()
	{
		return (int) this._power;
	}

	@Override
	public int getBaseMAtk()
	{
		return (int) this._power;
	}

	@Override
	public boolean validateConditions(Cubic cubic, Creature owner, WorldObject target)
	{
		if (this._conditions.isEmpty())
		{
			return true;
		}
		for (ICubicCondition condition : this._conditions)
		{
			if (!condition.test(cubic, owner, target))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void addCondition(ICubicCondition condition)
	{
		this._conditions.add(condition);
	}

	@Override
	public String toString()
	{
		return "Cubic id: " + this._id + " level: " + this._level + " slot: " + this._slot + " duration: " + this._duration + " delay: " + this._delay + " maxCount: " + this._maxCount + " useUp: " + this._useUp + " power: " + this._power + System.lineSeparator() + "skills: " + this._skills + System.lineSeparator() + "conditions:" + this._conditions + System.lineSeparator();
	}
}
