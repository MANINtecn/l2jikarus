package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ItemAuctionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.itemauction.ItemAuction;
import net.sf.l2jdev.gameserver.model.itemauction.ItemAuctionInstance;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;

public class RequestBidItemAuction extends ClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected void readImpl()
	{
		this._instanceId = this.readInt();
		this._bid = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You are bidding too fast.");
			}
			else if (this._bid >= 0L && this._bid <= Inventory.MAX_ADENA)
			{
				ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(this._instanceId);
				if (instance != null)
				{
					ItemAuction auction = instance.getCurrentAuction();
					if (auction != null)
					{
						auction.registerBid(player, this._bid);
					}
				}
			}
		}
	}
}
