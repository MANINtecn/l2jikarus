package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class TerrainObject extends Npc
{
	public TerrainObject(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.TerrainObject);
	}

	@Override
	public void onAction(Player player, boolean interact)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(Player player)
	{
		if (player.isGM())
		{
			super.onActionShift(player);
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
