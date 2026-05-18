package net.sf.l2jdev.gameserver.network.clientpackets.commission;

import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExCloseCommission;

public class RequestCommissionDelete extends ClientPacket
{
	private long _commissionId;

	@Override
	protected void readImpl()
	{
		this._commissionId = this.readLong();
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
				ItemCommissionManager.getInstance().deleteItem(player, this._commissionId);
			}
		}
	}
}
