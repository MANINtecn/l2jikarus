package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ItemAuctionManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.itemauction.ItemAuction;
import net.sf.l2jdev.gameserver.model.itemauction.ItemAuctionInstance;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAuctionInfoPacket;

public class RequestInfoItemAuction extends ClientPacket
{
	private int _instanceId;

	@Override
	protected void readImpl()
	{
		this._instanceId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this.getClient().getFloodProtectors().canUseItemAuction())
			{
				ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(this._instanceId);
				if (instance != null)
				{
					ItemAuction auction = instance.getCurrentAuction();
					if (auction != null)
					{
						player.updateLastItemAuctionRequest();
						player.sendPacket(new ExItemAuctionInfoPacket(true, auction, instance.getNextAuction()));
					}
				}
			}
		}
	}
}
