package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ClanWarehouse;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerWarehouse;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class SendWareHouseWithDrawList extends ClientPacket
{
	 
	private ItemHolder[] _items = null;

	@Override
	protected void readImpl()
	{
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 12 == this.remaining())
		{
			this._items = new ItemHolder[count];

			for (int i = 0; i < count; i++)
			{
				int objId = this.readInt();
				long cnt = this.readLong();
				if (objId < 1 || cnt < 0L)
				{
					this._items = null;
					return;
				}

				this._items[i] = new ItemHolder(objId, cnt);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		if (this._items != null)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (!this.getClient().getFloodProtectors().canPerformTransaction())
				{
					player.sendMessage("You are withdrawing items too fast.");
				}
				else
				{
					ItemContainer warehouse = player.getActiveWarehouse();
					if (warehouse != null)
					{
						if (!(warehouse instanceof PlayerWarehouse) && !player.getAccessLevel().allowTransaction())
						{
							player.sendMessage("Transactions are disabled for your Access Level.");
						}
						else if (PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE || player.getReputation() >= 0)
						{
							if (PlayerConfig.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
							{
								if (warehouse instanceof ClanWarehouse && !player.hasAccess(ClanAccess.ACCESS_WAREHOUSE))
								{
									return;
								}
							}
							else if (warehouse instanceof ClanWarehouse && !player.isClanLeader())
							{
								player.sendPacket(SystemMessageId.TO_STORE_AN_ITEM_AT_THE_CLAN_WAREHOUSE_YOU_HAVE_TO_PAY_A_FEE_IN_L_COINS_ONLY_THE_CLAN_LEADER_CAN_WITHDRAW_ITEMS_FROM_THERE_CONTINUE);
								return;
							}

							int weight = 0;
							int slots = 0;

							for (ItemHolder i : this._items)
							{
								Item item = warehouse.getItemByObjectId(i.getId());
								if (item == null || item.getCount() < i.getCount())
								{
									PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to withdraw non-existent item from warehouse.", GeneralConfig.DEFAULT_PUNISH);
									return;
								}

								weight = (int) (weight + i.getCount() * item.getTemplate().getWeight());
								if (!item.isStackable())
								{
									slots = (int) (slots + i.getCount());
								}
								else if (player.getInventory().getItemByItemId(item.getId()) == null)
								{
									slots++;
								}
							}

							if (!player.getInventory().validateCapacity(slots))
							{
								player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
							}
							else if (!player.getInventory().validateWeight(weight))
							{
								player.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
							}
							else
							{
								for (ItemHolder i : this._items)
								{
									Item oldItem = warehouse.getItemByObjectId(i.getId());
									if (oldItem == null || oldItem.getCount() < i.getCount())
									{
										PacketLogger.warning("Error withdrawing a warehouse object for char " + player.getName() + " (olditem == null)");
										return;
									}

									Item newItem = warehouse.transferItem(ItemProcessType.TRANSFER, i.getId(), i.getCount(), player.getInventory(), player, player.getLastFolkNPC());
									if (newItem == null)
									{
										PacketLogger.warning("Error withdrawing a warehouse object for char " + player.getName() + " (newitem == null)");
										return;
									}
								}

								player.sendItemList();
							}
						}
					}
				}
			}
		}
	}
}
