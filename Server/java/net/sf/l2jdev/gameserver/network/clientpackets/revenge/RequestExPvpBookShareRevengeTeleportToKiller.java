package net.sf.l2jdev.gameserver.network.clientpackets.revenge;

import net.sf.l2jdev.gameserver.managers.RevengeHistoryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestExPvpBookShareRevengeTeleportToKiller extends ClientPacket
{
	private String _victimName;
	private String _killerName;

	@Override
	protected void readImpl()
	{
		this._victimName = this.readSizedString();
		this._killerName = this.readSizedString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._victimName.equals(player.getName()))
			{
				RevengeHistoryManager.getInstance().teleportToKiller(player, this._killerName);
			}
		}
	}
}
