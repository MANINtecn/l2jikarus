package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class TradeDone extends ClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		this._response = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You are trading too fast.");
			}
			else
			{
				TradeList trade = player.getActiveTradeList();
				if (trade != null)
				{
					if (!trade.isLocked())
					{
						if (this._response == 1)
						{
							if (trade.getPartner() == null || World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null)
							{
								player.cancelActiveTrade();
								player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
								return;
							}

							if (trade.getOwner().hasItemRequest() || trade.getPartner().hasItemRequest())
							{
								return;
							}

							if (!player.getAccessLevel().allowTransaction())
							{
								player.cancelActiveTrade();
								player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
								return;
							}

							if ((player.getInstanceWorld() != trade.getPartner().getInstanceWorld()) || (player.calculateDistance3D(trade.getPartner()) > 150.0))
							{
								player.cancelActiveTrade();
								return;
							}

							trade.confirm();
						}
						else
						{
							player.cancelActiveTrade();
						}
					}
				}
			}
		}
	}
}
