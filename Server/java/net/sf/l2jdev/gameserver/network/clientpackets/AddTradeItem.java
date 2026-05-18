package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.TradeOtherAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.TradeOwnAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.TradeUpdate;

public class AddTradeItem extends ClientPacket
{
	private int _tradeId;
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		this._tradeId = this.readInt();
		this._objectId = this.readInt();
		this._count = this.readLong();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._count >= 1L)
			{
				TradeList trade = player.getActiveTradeList();
				if (trade == null)
				{
					PacketLogger.warning("Character: " + player.getName() + " requested item:" + this._objectId + " add without active tradelist:" + this._tradeId);
				}
				else
				{
					Player partner = trade.getPartner();
					if (partner != null && World.getInstance().getPlayer(partner.getObjectId()) != null && partner.getActiveTradeList() != null)
					{
						if (trade.isConfirmed() || partner.getActiveTradeList().isConfirmed())
						{
							player.sendPacket(SystemMessageId.YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED);
						}
						else if (!player.getAccessLevel().allowTransaction())
						{
							player.sendMessage("Transactions are disabled for your Access Level.");
							player.cancelActiveTrade();
						}
						else if (!player.validateItemManipulation(this._objectId, ItemProcessType.TRANSFER))
						{
							player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							Item item1 = player.getInventory().getItemByObjectId(this._objectId);
							TradeItem item2 = trade.addItem(this._objectId, this._count);
							if (item2 != null)
							{
								player.sendPacket(new TradeOwnAdd(1, item2));
								player.sendPacket(new TradeOwnAdd(2, item2));
								player.sendPacket(new TradeUpdate(1, null, null, 0L));
								player.sendPacket(new TradeUpdate(2, player, item2, item1.getCount() - item2.getCount()));
								partner.sendPacket(new TradeOtherAdd(1, item2));
								partner.sendPacket(new TradeOtherAdd(2, item2));
							}
						}
					}
					else
					{
						if (partner != null)
						{
							PacketLogger.warning("Character:" + player.getName() + " requested invalid trade object: " + this._objectId);
						}

						player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
						player.cancelActiveTrade();
					}
				}
			}
		}
	}
}
