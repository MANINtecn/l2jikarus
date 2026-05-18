package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExShowCommission;

public class CommissionManager extends Npc
{
	public CommissionManager(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.CommissionManager);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() ? true : super.isAutoAttackable(attacker);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.equalsIgnoreCase("show_commission"))
		{
			player.sendPacket(ExShowCommission.STATIC_PACKET);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
