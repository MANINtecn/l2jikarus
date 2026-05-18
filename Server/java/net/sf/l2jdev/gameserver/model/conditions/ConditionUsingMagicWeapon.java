package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionUsingMagicWeapon extends Condition
{
	private final boolean _value;

	public ConditionUsingMagicWeapon(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effected != null && effected.isPlayer())
		{
			ItemTemplate weapon = effected.getActiveWeaponItem();
			return weapon != null && weapon.isMagicWeapon() == this._value;
		}
		return false;
	}
}
