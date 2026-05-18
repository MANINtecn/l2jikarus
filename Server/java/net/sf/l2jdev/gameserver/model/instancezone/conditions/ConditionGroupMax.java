package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import java.util.List;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionGroupMax extends Condition
{
	public ConditionGroupMax(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, true, showMessageAndHtml);
		this.setSystemMessage(SystemMessageId.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
	}

	@Override
	protected boolean test(Player player, Npc npc, List<Player> group)
	{
		return group.size() <= this.getLimit();
	}

	public int getLimit()
	{
		return this.getParameters().getInt("limit");
	}
}
