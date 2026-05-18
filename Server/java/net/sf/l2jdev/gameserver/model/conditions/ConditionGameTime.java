package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public class ConditionGameTime extends Condition
{
	private final boolean _required;

	public ConditionGameTime(boolean required)
	{
		this._required = required;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return GameTimeTaskManager.getInstance().isNight() == this._required;
	}
}
