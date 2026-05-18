package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

public class BroadcastingTower extends Npc
{
	public BroadcastingTower(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.BroadcastingTower);
	}

	@Override
	public void showChatWindow(Player player, int value)
	{
		String filename = null;
		if (!this.isInsideRadius2D(-79884, 86529, 0, 50) && !this.isInsideRadius2D(-78858, 111358, 0, 50) && !this.isInsideRadius2D(-76973, 87136, 0, 50) && !this.isInsideRadius2D(-75850, 111968, 0, 50))
		{
			if (value == 0)
			{
				filename = "data/html/observation/" + this.getId() + ".htm";
			}
			else
			{
				filename = "data/html/observation/" + this.getId() + "-" + value + ".htm";
			}
		}
		else if (value == 0)
		{
			filename = "data/html/observation/" + this.getId() + "-Oracle.htm";
		}
		else
		{
			filename = "data/html/observation/" + this.getId() + "-Oracle-" + value + ".htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(player, filename);
		html.replace("%objectId%", String.valueOf(this.getObjectId()));
		player.sendPacket(html);
	}
}
