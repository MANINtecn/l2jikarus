package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerHasSummon extends Condition
{
	private final boolean _hasSummon;

	public ConditionPlayerHasSummon(boolean hasSummon)
	{
		this._hasSummon = hasSummon;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		return player == null ? false : this._hasSummon == player.hasSummon();
	}
}
