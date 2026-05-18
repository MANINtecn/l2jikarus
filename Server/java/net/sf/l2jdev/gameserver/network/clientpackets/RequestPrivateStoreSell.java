package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.model.ItemRequest;
import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestPrivateStoreSell extends ClientPacket
{
	private int _storePlayerId;
	private ItemRequest[] _items = null;

	@Override
	protected void readImpl()
	{
		this._storePlayerId = this.readInt();
		int itemsCount = this.readInt();
		if (itemsCount > 0 && itemsCount <= PlayerConfig.MAX_ITEM_IN_PACKET)
		{
			this._items = new ItemRequest[itemsCount];

			for (int i = 0; i < itemsCount; i++)
			{
				int slot = this.readInt();
				int itemId = this.readInt();
				this.readShort();
				this.readShort();
				long count = this.readLong();
				long price = this.readLong();
				this.readInt();
				this.readInt();
				this.readInt();
				int soulCrystals = this.readByte();

				for (int s = 0; s < soulCrystals; s++)
				{
					this.readInt();
				}

				int soulCrystals2 = this.readByte();

				for (int s = 0; s < soulCrystals2; s++)
				{
					this.readInt();
				}

				if (itemId < 1 || count < 1L || price < 0L)
				{
					this._items = null;
					return;
				}

				this._items[i] = new ItemRequest(slot, itemId, count, price);
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
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (player.isRegisteredOnEvent())
			{
				player.sendMessage("You cannot open a private store while participating in an event.");
			}
			else if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You are selling items too fast.");
			}
			else
			{
				Player storePlayer = World.getInstance().getPlayer(this._storePlayerId);
				if (storePlayer != null && player.isInsideRadius3D(storePlayer, 250))
				{
					if (player.getInstanceWorld() == storePlayer.getInstanceWorld())
					{
						if (storePlayer.getPrivateStoreType() == PrivateStoreType.BUY && !player.isCursedWeaponEquipped())
						{
							TradeList storeList = storePlayer.getBuyList();
							if (storeList != null)
							{
								if (!player.getAccessLevel().allowTransaction())
								{
									player.sendMessage("Transactions are disabled for your Access Level.");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								else if (!storeList.privateStoreSell(player, this._items))
								{
									player.sendPacket(ActionFailed.STATIC_PACKET);
									PacketLogger.warning("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
								}
								else
								{
									if (OfflineTradeConfig.OFFLINE_TRADE_ENABLE && OfflineTradeConfig.STORE_OFFLINE_TRADE_IN_REALTIME && (storePlayer.getClient() == null || storePlayer.getClient().isDetached()))
									{
										OfflineTraderTable.getInstance().onTransaction(storePlayer, storeList.getItemCount() == 0, false);
									}

									if (storeList.getItemCount() == 0)
									{
										storePlayer.setPrivateStoreType(PrivateStoreType.NONE);
										storePlayer.broadcastUserInfo();
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
