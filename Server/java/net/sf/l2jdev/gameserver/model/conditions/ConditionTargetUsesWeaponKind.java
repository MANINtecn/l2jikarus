package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetUsesWeaponKind extends Condition
{
	private final int _weaponMask;

	public ConditionTargetUsesWeaponKind(int weaponMask)
	{
		this._weaponMask = weaponMask;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effected == null)
		{
			return false;
		}
		Weapon weapon = effected.getActiveWeaponItem();
		return weapon == null ? false : (weapon.getItemType().mask() & this._weaponMask) != 0;
	}
}
