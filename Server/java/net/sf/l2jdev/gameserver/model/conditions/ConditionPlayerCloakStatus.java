package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerCloakStatus extends Condition
{
	private final boolean _value;

	public ConditionPlayerCloakStatus(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector.isPlayer() && effector.asPlayer().getInventory().canEquipCloak() == this._value;
	}
}
