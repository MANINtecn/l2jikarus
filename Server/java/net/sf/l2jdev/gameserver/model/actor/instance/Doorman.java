package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.TeleporterData;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.teleporter.TeleportHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;

public class Doorman extends Folk
{
	public Doorman(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Doorman);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() ? true : super.isAutoAttackable(attacker);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Chat"))
		{
			this.showChatWindow(player);
		}
		else if (command.startsWith("open_doors"))
		{
			if (this.isOwnerClan(player))
			{
				if (this.isUnderSiege())
				{
					this.cannotManageDoors(player);
				}
				else
				{
					this.openDoors(player, command);
				}
			}
		}
		else if (command.startsWith("close_doors"))
		{
			if (this.isOwnerClan(player))
			{
				if (this.isUnderSiege())
				{
					this.cannotManageDoors(player);
				}
				else
				{
					this.closeDoors(player, command);
				}
			}
		}
		else if (command.startsWith("tele"))
		{
			if (this.isOwnerClan(player))
			{
				TeleportHolder holder = TeleporterData.getInstance().getHolder(this.getId(), TeleportType.OTHER.name());
				if (holder != null)
				{
					int locId = Integer.parseInt(command.substring(5).trim());
					holder.doTeleport(player, this, locId);
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		if (!this.isOwnerClan(player))
		{
			html.setFile(player, "data/html/doorman/" + this.getTemplate().getId() + "-no.htm");
		}
		else
		{
			html.setFile(player, "data/html/doorman/" + this.getTemplate().getId() + ".htm");
		}

		html.replace("%objectId%", String.valueOf(this.getObjectId()));
		player.sendPacket(html);
	}

	protected void openDoors(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
		st.nextToken();

		while (st.hasMoreTokens())
		{
			DoorData.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
		}
	}

	protected void closeDoors(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
		st.nextToken();

		while (st.hasMoreTokens())
		{
			DoorData.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
		}
	}

	protected void cannotManageDoors(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(player, "data/html/doorman/" + this.getTemplate().getId() + "-busy.htm");
		player.sendPacket(html);
	}

	protected boolean isOwnerClan(Player player)
	{
		return true;
	}

	protected boolean isUnderSiege()
	{
		return false;
	}
}
