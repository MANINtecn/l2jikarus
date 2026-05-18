package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionReenter extends Condition
{
	public ConditionReenter(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		this.setSystemMessage(SystemMessageId.C1_CANNOT_ENTER_YET, (message, player) -> message.addString(player.getName()));
	}

	@Override
	protected boolean test(Player player, Npc npc)
	{
		int instanceId = this.getParameters().getInt("instanceId", this.getInstanceTemplate().getId());
		return System.currentTimeMillis() > InstanceManager.getInstance().getInstanceTime(player, instanceId);
	}
}
