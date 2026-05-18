package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionDistance extends Condition
{
	public ConditionDistance(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		this.setSystemMessage(SystemMessageId.C1_IS_TOO_FAR_FROM_THE_INSTANCE_ZONE_ENTRANCE, (message, player) -> message.addString(player.getName()));
	}

	@Override
	public boolean test(Player player, Npc npc)
	{
		int distance = this.getParameters().getInt("distance", 1000);
		return player.isInsideRadius3D(npc, distance);
	}
}
