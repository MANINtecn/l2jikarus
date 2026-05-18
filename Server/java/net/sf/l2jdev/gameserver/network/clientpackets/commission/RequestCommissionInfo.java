package net.sf.l2jdev.gameserver.network.clientpackets.commission;

import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExCloseCommission;
import net.sf.l2jdev.gameserver.network.serverpackets.commission.ExResponseCommissionInfo;

public class RequestCommissionInfo extends ClientPacket
{
	private int _itemObjectId;

	@Override
	protected void readImpl()
	{
		this._itemObjectId = this.readInt();
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
				Item itemInstance = player.getInventory().getItemByObjectId(this._itemObjectId);
				if (itemInstance != null)
				{
					player.sendPacket(player.getLastCommissionInfos().getOrDefault(itemInstance.getId(), ExResponseCommissionInfo.EMPTY));
				}
				else
				{
					player.sendPacket(ExResponseCommissionInfo.EMPTY);
				}
			}
		}
	}
}
