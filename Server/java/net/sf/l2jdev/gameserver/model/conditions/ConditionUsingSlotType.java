package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionUsingSlotType extends Condition
{
	private final int _mask;

	public ConditionUsingSlotType(int mask)
	{
		this._mask = mask;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Weapon activeWeapon = effector.getActiveWeaponItem();
			return activeWeapon == null ? false : (activeWeapon.getBodyPart().getMask() & this._mask) != 0L;
		}
		return false;
	}
}
