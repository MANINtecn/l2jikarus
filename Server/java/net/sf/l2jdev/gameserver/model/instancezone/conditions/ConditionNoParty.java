package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;

public class ConditionNoParty extends Condition
{
	public ConditionNoParty(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, true, showMessageAndHtml);
	}

	@Override
	public boolean test(Player player, Npc npc)
	{
		return !player.isInParty();
	}
}
