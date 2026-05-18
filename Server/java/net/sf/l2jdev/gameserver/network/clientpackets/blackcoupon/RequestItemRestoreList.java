package net.sf.l2jdev.gameserver.network.clientpackets.blackcoupon;

import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.BlackCouponRestoreCategory;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.blackcoupon.ItemRestoreList;

public class RequestItemRestoreList extends ClientPacket
{
	private short _category;

	@Override
	public void readImpl()
	{
		this._category = this.readByte();
	}

	@Override
	public void runImpl()
	{
		if (BlackCouponManager.getInstance().getEventStatus())
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				BlackCouponRestoreCategory requestedCategory = BlackCouponRestoreCategory.getCategoryById(this._category);
				player.sendPacket(new ItemRestoreList(player.getObjectId(), requestedCategory));
			}
		}
	}
}
