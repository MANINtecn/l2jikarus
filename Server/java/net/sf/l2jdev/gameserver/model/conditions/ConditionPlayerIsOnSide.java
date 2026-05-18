package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.CastleSide;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerIsOnSide extends Condition
{
	private final CastleSide _side;

	public ConditionPlayerIsOnSide(CastleSide side)
	{
		this._side = side;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector != null && effector.isPlayer() ? effector.asPlayer().getPlayerSide() == this._side : false;
	}
}
