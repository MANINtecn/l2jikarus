package net.sf.l2jdev.gameserver.model.conditions;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerHasClanHall extends Condition
{
	private final List<Integer> _clanHall;

	public ConditionPlayerHasClanHall(List<Integer> clanHall)
	{
		this._clanHall = clanHall;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (!effector.isPlayer())
		{
			return false;
		}
		Clan clan = effector.asPlayer().getClan();
		if (clan != null)
		{
			return this._clanHall.size() == 1 && this._clanHall.get(0) == -1 ? clan.getHideoutId() > 0 : this._clanHall.contains(clan.getHideoutId());
		}
		return this._clanHall.size() == 1 && this._clanHall.get(0) == 0;
	}
}
