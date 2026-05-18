package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.model.residences.ResidenceType;

public class ConditionHasResidence extends Condition
{
	public ConditionHasResidence(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
	}

	@Override
	protected boolean test(Player player, Npc npc)
	{
		Clan clan = player.getClan();
		if (clan == null)
		{
			return false;
		}
		boolean test = false;
		StatSet params = this.getParameters();
		int id = params.getInt("id");
		ResidenceType type = params.getEnum("type", ResidenceType.class);
		switch (type)
		{
			case CASTLE:
				test = clan.getCastleId() == id;
				break;
			case FORTRESS:
				test = clan.getFortId() == id;
				break;
			case CLANHALL:
				test = clan.getHideoutId() == id;
		}

		return test;
	}
}
