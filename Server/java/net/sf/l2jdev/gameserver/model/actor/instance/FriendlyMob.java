package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class FriendlyMob extends Attackable
{
	public FriendlyMob(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.FriendlyMob);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isPlayer() ? attacker.asPlayer().getReputation() < 0 : super.isAutoAttackable(attacker);
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}
}
