package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerInInstance extends Condition
{
	public boolean _inInstance;

	public ConditionPlayerInInstance(boolean inInstance)
	{
		this._inInstance = inInstance;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector.asPlayer() == null)
		{
			return false;
		}
		return effector.getInstanceId() == 0 ? !this._inInstance : this._inInstance;
	}
}
