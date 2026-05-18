package net.sf.l2jdev.gameserver.network.clientpackets.commission;

import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExCloseCommission;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionItemList;

public class RequestCommissionRegistrableItemList extends ClientPacket
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
			if (!ItemCommissionManager.isPlayerAllowedToInteract(player))
			{
				player.sendPacket(ExCloseCommission.STATIC_PACKET);
			}
			else
			{
				player.sendPacket(new ExResponseCommissionItemList(1, player.getInventory().getAvailableItems(false, false, false)));
				player.sendPacket(new ExResponseCommissionItemList(2, player.getInventory().getAvailableItems(false, false, false)));
			}
		}
	}
}
