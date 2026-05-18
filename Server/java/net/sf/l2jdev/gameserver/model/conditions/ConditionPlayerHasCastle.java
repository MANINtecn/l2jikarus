package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerHasCastle extends Condition
{
	private final int _castle;

	public ConditionPlayerHasCastle(int castle)
	{
		this._castle = castle;
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
			return this._castle == 0;
		}
		return this._castle == -1 ? clan.getCastleId() > 0 : clan.getCastleId() == this._castle;
	}
}
