package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerHasFort extends Condition
{
	private final int _fort;

	public ConditionPlayerHasFort(int fort)
	{
		this._fort = fort;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (!effector.isPlayer())
		{
			return false;
		}
		Clan clan = effector.asPlayer().getClan();
		if (clan == null)
		{
			return this._fort == 0;
		}
		return this._fort == -1 ? clan.getFortId() > 0 : clan.getFortId() == this._fort;
	}
}
