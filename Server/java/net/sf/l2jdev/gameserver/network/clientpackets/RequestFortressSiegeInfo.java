package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort != null && fort.getSiege().isInProgress())
				{
					player.sendPacket(new ExShowFortressSiegeInfo(fort));
				}
			}
		}
	}
}
