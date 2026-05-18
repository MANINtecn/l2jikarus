package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetNpcType extends Condition
{
	private final InstanceType[] _npcType;

	public ConditionTargetNpcType(InstanceType[] type)
	{
		this._npcType = type;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effected == null ? false : effected.getInstanceType().isTypes(this._npcType);
	}
}
