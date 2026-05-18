package net.sf.l2jdev.gameserver.network.clientpackets.commission;

import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.commission.CommissionItem;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExCloseCommission;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionBuyInfo;

public class RequestCommissionBuyInfo extends ClientPacket
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
			else if (player.isInventoryUnder80(false) && player.getWeightPenalty() < 3)
			{
				CommissionItem commissionItem = ItemCommissionManager.getInstance().getCommissionItem(this._commissionId);
				if (commissionItem != null)
				{
					player.sendPacket(new ExResponseCommissionBuyInfo(commissionItem));
				}
				else
				{
					player.sendPacket(SystemMessageId.ITEM_PURCHASE_IS_NOT_AVAILABLE_BECAUSE_THE_CORRESPONDING_ITEM_DOES_NOT_EXIST);
					player.sendPacket(ExResponseCommissionBuyInfo.FAILED);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.TO_BUY_CANCEL_YOU_NEED_TO_FREE_20_OF_WEIGHT_AND_10_OF_SLOTS_IN_YOUR_INVENTORY);
				player.sendPacket(ExResponseCommissionBuyInfo.FAILED);
			}
		}
	}
}
