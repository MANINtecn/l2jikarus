package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestSaveBookMarkSlot extends ClientPacket
{
	private int icon;
	private String name;
	private String tag;

	@Override
	protected void readImpl()
	{
		this.name = this.readString();
		this.icon = this.readInt();
		this.tag = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isInTimedHuntingZone())
			{
				player.sendMessage("You cannot bookmark this location.");
			}
			else
			{
				player.teleportBookmarkAdd(player.getX(), player.getY(), player.getZ(), this.icon, this.tag, this.name);
			}
		}
	}
}
