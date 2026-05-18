package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerFreight;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;

public class RequestPackageSend extends ClientPacket
{
 
	private ItemHolder[] _items = null;
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
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
		Player player = this.getPlayer();
		if (this._items != null && player != null && player.getAccountChars().containsKey(this._objectId))
		{
			if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You depositing items too fast.");
			}
			else if (player.hasItemRequest())
			{
				PunishmentManager.handleIllegalPlayerAction(player, player + " tried to use enchant exploit!", GeneralConfig.DEFAULT_PUNISH);
			}
			else if (player.getActiveTradeList() == null)
			{
				if (PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE || player.getReputation() >= 0)
				{
					int fee = this._items.length * PlayerConfig.ALT_FREIGHT_PRICE;
					long currentAdena = player.getAdena();
					int slots = 0;
					ItemContainer warehouse = new PlayerFreight(this._objectId);

					for (ItemHolder i : this._items)
					{
						Item item = player.checkItemManipulation(i.getId(), i.getCount(), "freight");
						if (item == null)
						{
							PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
							warehouse.deleteMe();
							return;
						}

						if (!item.isFreightable())
						{
							warehouse.deleteMe();
							return;
						}

						if (item.getId() == 57)
						{
							currentAdena -= i.getCount();
						}
						else if (!item.isStackable())
						{
							slots = (int) (slots + i.getCount());
						}
						else if (warehouse.getItemByItemId(item.getId()) == null)
						{
							slots++;
						}
					}

					if (!warehouse.validateCapacity(slots))
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
						warehouse.deleteMe();
					}
					else if (currentAdena >= fee && player.reduceAdena(ItemProcessType.FEE, fee, player, false))
					{
						InventoryUpdate playerIU = new InventoryUpdate();

						for (ItemHolder i : this._items)
						{
							Item oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
							if (oldItem == null)
							{
								PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
								warehouse.deleteMe();
								return;
							}

							Item newItem = player.getInventory().transferItem(ItemProcessType.TRANSFER, i.getId(), i.getCount(), warehouse, player, null);
							if (newItem == null)
							{
								PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
							}
							else
							{
								if (oldItem.getCount() > 0L && oldItem != newItem)
								{
									playerIU.addModifiedItem(oldItem);
								}
								else
								{
									playerIU.addRemovedItem(oldItem);
								}

								World.getInstance().removeObject(oldItem);
								World.getInstance().removeObject(newItem);
							}
						}

						warehouse.deleteMe();
						player.sendInventoryUpdate(playerIU);
					}
					else
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
						warehouse.deleteMe();
					}
				}
			}
		}
	}
}
