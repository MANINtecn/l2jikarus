package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerCp extends Condition
{
	private final int _cp;

	public ConditionPlayerCp(int cp)
	{
		this._cp = cp;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector != null && effector.getCurrentCp() * 100.0 / effector.getMaxCp() >= this._cp;
	}
}
