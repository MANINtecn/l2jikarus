package net.sf.l2jdev.gameserver.network.clientpackets.revenge;

import net.sf.l2jdev.gameserver.managers.RevengeHistoryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestExPvpBookShareRevengeKillerLocation extends ClientPacket
{
	private String _killerName;

	@Override
	protected void readImpl()
	{
		this.readSizedString();
		this._killerName = this.readSizedString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			RevengeHistoryManager.getInstance().locateKiller(player, this._killerName);
		}
	}
}
