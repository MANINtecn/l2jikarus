package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreManageListSell;
import net.sf.l2jdev.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class SetPrivateStoreListSell extends ClientPacket
{
	private boolean _packageSale;
	private SetPrivateStoreListSell.Item[] _items = null;

	@Override
	protected void readImpl()
	{
		this._packageSale = this.readInt() == 1;
		int count = this.readInt();
		if (count >= 1 && count <= PlayerConfig.MAX_ITEM_IN_PACKET)
		{
			this._items = new SetPrivateStoreListSell.Item[count];

			for (int i = 0; i < count; i++)
			{
				int itemId = this.readInt();
				long cnt = this.readLong();
				long price = this.readLong();
				if (itemId < 1 || cnt < 1L || price < 0L)
				{
					this._items = null;
					return;
				}

				this.readString();
				this._items[i] = new SetPrivateStoreListSell.Item(itemId, cnt, price);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._items == null)
			{
				player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
				player.setPrivateStoreType(PrivateStoreType.NONE);
				player.broadcastUserInfo();
			}
			else if (!player.getAccessLevel().allowTransaction())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			}
			else if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !player.isInDuel())
			{
				if (player.isInsideZone(ZoneId.NO_STORE))
				{
					player.sendPacket(new PrivateStoreManageListSell(1, player, this._packageSale));
					player.sendPacket(new PrivateStoreManageListSell(2, player, this._packageSale));
					player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (this._items.length > player.getPrivateSellStoreLimit())
				{
					player.sendPacket(new PrivateStoreManageListSell(1, player, this._packageSale));
					player.sendPacket(new PrivateStoreManageListSell(2, player, this._packageSale));
					player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				}
				else
				{
					TradeList tradeList = player.getSellList();
					tradeList.clear();
					tradeList.setPackaged(this._packageSale);
					long totalCost = player.getAdena();

					for (SetPrivateStoreListSell.Item i : this._items)
					{
						if (!i.addToTradeList(tradeList))
						{
							player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_YOUR_OUT_OF_POCKET_ADENA_LIMIT);
							return;
						}

						totalCost += i.getPrice();
						if (totalCost > Inventory.MAX_ADENA)
						{
							player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_YOUR_OUT_OF_POCKET_ADENA_LIMIT);
							return;
						}
					}

					player.sitDown();
					if (this._packageSale)
					{
						player.setPrivateStoreType(PrivateStoreType.PACKAGE_SELL);
					}
					else
					{
						player.setPrivateStoreType(PrivateStoreType.SELL);
					}

					player.broadcastUserInfo();
					if (this._packageSale)
					{
						player.broadcastPacket(new ExPrivateStoreSetWholeMsg(player));
					}
					else
					{
						player.broadcastPacket(new PrivateStoreMsgSell(player));
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
				player.sendPacket(new PrivateStoreManageListSell(1, player, this._packageSale));
				player.sendPacket(new PrivateStoreManageListSell(2, player, this._packageSale));
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	private static class Item
	{
		private final int _objectId;
		private final long _count;
		private final long _price;

		public Item(int objectId, long count, long price)
		{
			this._objectId = objectId;
			this._count = count;
			this._price = price;
		}

		public boolean addToTradeList(TradeList list)
		{
			if (Inventory.MAX_ADENA / this._count < this._price)
			{
				return false;
			}
			list.addItem(this._objectId, this._count, this._price);
			return true;
		}

		public long getPrice()
		{
			return this._count * this._price;
		}
	}
}
