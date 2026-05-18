package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerPledgeClass extends Condition
{
	private final int _pledgeClass;

	public ConditionPlayerPledgeClass(int pledgeClass)
	{
		this._pledgeClass = pledgeClass;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		if (player != null && player.getClan() != null)
		{
			boolean isClanLeader = player.isClanLeader();
			return this._pledgeClass == -1 && !isClanLeader ? false : isClanLeader || player.getPledgeClass() >= this._pledgeClass;
		}
		return false;
	}
}
