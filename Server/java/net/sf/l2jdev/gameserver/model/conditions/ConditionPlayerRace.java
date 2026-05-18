package net.sf.l2jdev.gameserver.model.conditions;

import java.util.Set;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerRace extends Condition
{
	private final Set<Race> _races;

	public ConditionPlayerRace(Set<Race> races)
	{
		this._races = races;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector != null && effector.isPlayer() ? this._races.contains(effector.asPlayer().getRace()) : false;
	}
}
