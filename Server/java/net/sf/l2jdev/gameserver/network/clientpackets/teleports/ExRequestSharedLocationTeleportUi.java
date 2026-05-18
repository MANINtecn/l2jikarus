package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import net.sf.l2jdev.gameserver.data.holders.SharedTeleportHolder;
import net.sf.l2jdev.gameserver.managers.SharedTeleportManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.teleports.ExShowSharedLocationTeleportUi;

public class ExRequestSharedLocationTeleportUi extends ClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		this._id = (this.readInt() - 1) / 256;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			SharedTeleportHolder teleport = SharedTeleportManager.getInstance().getTeleport(this._id);
			if (teleport != null)
			{
				player.sendPacket(new ExShowSharedLocationTeleportUi(teleport));
			}
		}
	}
}
