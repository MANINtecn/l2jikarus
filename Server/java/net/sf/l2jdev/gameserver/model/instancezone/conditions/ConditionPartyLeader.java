package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionPartyLeader extends Condition
{
	public ConditionPartyLeader(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, true, showMessageAndHtml);
		this.setSystemMessage(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
	}

	@Override
	public boolean test(Player player, Npc npc)
	{
		return player.isInParty() && player.getParty().isLeader(player);
	}
}
