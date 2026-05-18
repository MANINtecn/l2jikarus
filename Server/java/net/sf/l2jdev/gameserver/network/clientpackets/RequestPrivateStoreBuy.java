package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.HashSet;
import java.util.Set;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.ItemRequest;
import net.sf.l2jdev.gameserver.model.TradeList;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestPrivateStoreBuy extends ClientPacket
{
 
	private int _storePlayerId;
	private Set<ItemRequest> _items = null;

	@Override
	protected void readImpl()
	{
		this._storePlayerId = this.readInt();
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 20 == this.remaining())
		{
			this._items = new HashSet<>();

			for (int i = 0; i < count; i++)
			{
				int objectId = this.readInt();
				long cnt = this.readLong();
				long price = this.readLong();
				if (objectId < 1 || cnt < 1L || price < 0L)
				{
					this._items = null;
					return;
				}

				this._items.add(new ItemRequest(objectId, cnt, price));
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
				player.sendMessage("You are buying items too fast.");
			}
			else
			{
				WorldObject object = World.getInstance().getPlayer(this._storePlayerId);
				if (object != null && !player.isCursedWeaponEquipped())
				{
					Player storePlayer = object.asPlayer();
					if (player.isInsideRadius3D(storePlayer, 250))
					{
						if (player.getInstanceWorld() == storePlayer.getInstanceWorld())
						{
							if (storePlayer.getPrivateStoreType() == PrivateStoreType.SELL || storePlayer.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)
							{
								TradeList storeList = storePlayer.getSellList();
								if (storeList != null)
								{
									if (!player.getAccessLevel().allowTransaction())
									{
										player.sendMessage("Transactions are disabled for your Access Level.");
										player.sendPacket(ActionFailed.STATIC_PACKET);
									}
									else if (storePlayer.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL && storeList.getItemCount() > this._items.size())
									{
										String msgErr = "[RequestPrivateStoreBuy] " + player + " tried to buy less items than sold by package-sell, ban this player for bot usage!";
										PunishmentManager.handleIllegalPlayerAction(player, msgErr, GeneralConfig.DEFAULT_PUNISH);
									}
									else
									{
										int result = storeList.privateStoreBuy(player, this._items);
										if (result > 0)
										{
											player.sendPacket(ActionFailed.STATIC_PACKET);
											if (result > 1)
											{
												PacketLogger.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
											}
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
	}
}
