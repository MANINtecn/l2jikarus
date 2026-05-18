package net.sf.l2jdev.gameserver.model.cubic;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.conditions.ICubicCondition;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class CubicSkill extends SkillHolder implements ICubicConditionHolder
{
	private final int _triggerRate;
	private final int _successRate;
	private final boolean _canUseOnStaticObjects;
	private final CubicTargetType _targetType;
	private final List<ICubicCondition> _conditions = new ArrayList<>();
	private final boolean _targetDebuff;

	public CubicSkill(StatSet set)
	{
		super(set.getInt("id"), set.getInt("level"));
		this._triggerRate = set.getInt("triggerRate", 100);
		this._successRate = set.getInt("successRate", 100);
		this._canUseOnStaticObjects = set.getBoolean("canUseOnStaticObjects", false);
		this._targetType = set.getEnum("target", CubicTargetType.class, CubicTargetType.TARGET);
		this._targetDebuff = set.getBoolean("targetDebuff", false);
	}

	public int getTriggerRate()
	{
		return this._triggerRate;
	}

	public int getSuccessRate()
	{
		return this._successRate;
	}

	public boolean canUseOnStaticObjects()
	{
		return this._canUseOnStaticObjects;
	}

	public CubicTargetType getTargetType()
	{
		return this._targetType;
	}

	public boolean isTargetingDebuff()
	{
		return this._targetDebuff;
	}

	@Override
	public boolean validateConditions(Cubic cubic, Creature owner, WorldObject target)
	{
		if (this._targetDebuff && target.isCreature() && target.asCreature().getEffectList().getDebuffCount() == 0)
		{
			return false;
		}
		else if (this._conditions.isEmpty())
		{
			return true;
		}
		else
		{
			for (ICubicCondition condition : this._conditions)
			{
				if (!condition.test(cubic, owner, target))
				{
					return false;
				}
			}

			return true;
		}
	}

	@Override
	public void addCondition(ICubicCondition condition)
	{
		this._conditions.add(condition);
	}

	@Override
	public String toString()
	{
		return "Cubic skill id: " + this.getSkillId() + " level: " + this.getSkillLevel() + " triggerRate: " + this._triggerRate + " successRate: " + this._successRate + " canUseOnStaticObjects: " + this._canUseOnStaticObjects + " targetType: " + this._targetType + " isTargetingDebuff: " + this._targetDebuff + System.lineSeparator();
	}
}
