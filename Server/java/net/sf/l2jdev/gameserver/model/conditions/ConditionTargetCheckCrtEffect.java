package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetCheckCrtEffect extends Condition
{
	private final boolean _isCrtEffect;

	public ConditionTargetCheckCrtEffect(boolean isCrtEffect)
	{
		this._isCrtEffect = isCrtEffect;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effected.isNpc() ? effected.asNpc().getTemplate().canBeCrt() == this._isCrtEffect : true;
	}
}
