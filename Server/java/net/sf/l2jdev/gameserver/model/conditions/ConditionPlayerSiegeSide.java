package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerSiegeSide extends Condition
{
	private final int _siegeSide;

	public ConditionPlayerSiegeSide(int side)
	{
		this._siegeSide = side;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		return player == null ? false : player.getSiegeSide() == this._siegeSide;
	}
}
