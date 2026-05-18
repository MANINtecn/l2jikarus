package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SoulType;

public class ConditionPlayerSouls extends Condition
{
	private final int _souls;
	private final SoulType _type;

	public ConditionPlayerSouls(int souls, SoulType type)
	{
		this._souls = souls;
		this._type = type;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector.isPlayer() && effector.asPlayer().getChargedSouls(this._type) >= this._souls;
	}
}
