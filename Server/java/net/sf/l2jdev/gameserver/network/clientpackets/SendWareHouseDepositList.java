package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerWarehouse;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;

public class SendWareHouseDepositList extends ClientPacket
{
	 
	private List<ItemHolder> _items = null;

	@Override
	protected void readImpl()
	{
		int size = this.readInt();
		if (size > 0 && size <= PlayerConfig.MAX_ITEM_IN_PACKET && size * 12 == this.remaining())
		{
			this._items = new ArrayList<>(size);

			for (int i = 0; i < size; i++)
			{
				int objId = this.readInt();
				long count = this.readLong();
				if (objId < 1 || count < 1L)
				{
					this._items = null;
					return;
				}

				this._items.add(new ItemHolder(objId, count));
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
					player.sendMessage("You are depositing items too fast.");
				}
				else
				{
					ItemContainer warehouse = player.getActiveWarehouse();
					if (warehouse != null)
					{
						Npc manager = player.getLastFolkNPC();
						if ((manager == null || !manager.isWarehouse() || !manager.canInteract(player)) && !player.isGM())
						{
							player.sendPacket(SystemMessageId.YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE);
						}
						else
						{
							boolean isPrivate = warehouse instanceof PlayerWarehouse;
							if (!isPrivate && !player.getAccessLevel().allowTransaction())
							{
								player.sendMessage("Transactions are disabled for your Access Level.");
							}
							else if (player.hasItemRequest())
							{
								PunishmentManager.handleIllegalPlayerAction(player, player + " tried to use enchant Exploit!", GeneralConfig.DEFAULT_PUNISH);
							}
							else if (PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE || player.getReputation() >= 0)
							{
								long fee = this._items.size() * 30;
								long currentAdena = player.getAdena();
								int slots = 0;

								for (ItemHolder itemHolder : this._items)
								{
									Item item = player.checkItemManipulation(itemHolder.getId(), itemHolder.getCount(), "deposit");
									if (item == null)
									{
										PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
										return;
									}

									if (item.getId() == 57)
									{
										currentAdena -= itemHolder.getCount();
									}

									if (!item.isStackable())
									{
										slots = (int) (slots + itemHolder.getCount());
									}
									else if (warehouse.getItemByItemId(item.getId()) == null)
									{
										slots++;
									}
								}

								if (!warehouse.validateCapacity(slots))
								{
									player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
								}
								else if (currentAdena >= fee && player.reduceAdena(ItemProcessType.FEE, fee, manager, false))
								{
									if (player.getActiveTradeList() == null)
									{
										InventoryUpdate playerIU = new InventoryUpdate();

										for (ItemHolder itemHolder : this._items)
										{
											Item oldItem = player.checkItemManipulation(itemHolder.getId(), itemHolder.getCount(), "deposit");
											if (oldItem == null)
											{
												PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
												return;
											}

											if (oldItem.isDepositable(isPrivate) && oldItem.isAvailable(player, true, isPrivate))
											{
												Item newItem = player.getInventory().transferItem(ItemProcessType.TRANSFER, itemHolder.getId(), itemHolder.getCount(), warehouse, player, manager);
												if (newItem == null)
												{
													PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
												}
												else if (oldItem.getCount() > 0L && oldItem != newItem)
												{
													playerIU.addModifiedItem(oldItem);
												}
												else
												{
													playerIU.addRemovedItem(oldItem);
												}
											}
										}

										player.sendInventoryUpdate(playerIU);
									}
								}
								else
								{
									player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
								}
							}
						}
					}
				}
			}
		}
	}
}
