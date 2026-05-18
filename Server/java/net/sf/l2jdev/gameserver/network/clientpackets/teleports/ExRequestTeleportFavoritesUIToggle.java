package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.teleports.ExTeleportFavoritesList;

public class ExRequestTeleportFavoritesUIToggle extends ClientPacket
{
	private boolean _enable;

	@Override
	protected void readImpl()
	{
		this._enable = this.readByte() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExTeleportFavoritesList(player, this._enable));
		}
	}
}
