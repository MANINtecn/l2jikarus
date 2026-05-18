package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionCommandChannel extends Condition
{
	public ConditionCommandChannel(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, true, showMessageAndHtml);
		this.setSystemMessage(SystemMessageId.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL);
	}

	@Override
	public boolean test(Player player, Npc npc)
	{
		return player.isInCommandChannel();
	}
}
