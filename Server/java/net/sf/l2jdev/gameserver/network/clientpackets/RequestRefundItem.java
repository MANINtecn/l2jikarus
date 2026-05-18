package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.MerchantZeroSellPriceConfig;
import net.sf.l2jdev.gameserver.data.xml.BuyListData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Merchant;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBuySellList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoInvenWeight;

public class RequestRefundItem extends ClientPacket
{
 
	private int _listId;
	private int[] _items = null;

	@Override
	protected void readImpl()
	{
		this._listId = this.readInt();
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 4 == this.remaining())
		{
			this._items = new int[count];

			for (int i = 0; i < count; i++)
			{
				this._items[i] = this.readInt();
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!this.getClient().getFloodProtectors().canPerformTransaction())
			{
				player.sendMessage("You are using refund too fast.");
			}
			else if (this._items != null && player.hasRefund())
			{
				WorldObject target = player.getTarget();
				Merchant merchant = null;
				if (!player.isGM() && this._listId != 423)
				{
					if (!(target instanceof Merchant) || !player.isInsideRadius3D(target, 250) || player.getInstanceId() != target.getInstanceId())
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					merchant = (Merchant) target;
				}

				if (merchant == null && !player.isGM() && this._listId != 423)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					ProductList buyList = BuyListData.getInstance().getBuyList(this._listId);
					if (buyList == null)
					{
						PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + this._listId, GeneralConfig.DEFAULT_PUNISH);
					}
					else if (merchant != null && !buyList.isNpcAllowed(merchant.getId()))
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						long weight = 0L;
						long adena = 0L;
						long slots = 0L;
						Item[] refund = player.getRefund().getItems().toArray(new Item[0]);
						int[] objectIds = new int[this._items.length];

						for (int i = 0; i < this._items.length; i++)
						{
							int idx = this._items[i];
							if (idx < 0 || idx >= refund.length)
							{
								PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent invalid refund index", GeneralConfig.DEFAULT_PUNISH);
								return;
							}

							for (int j = i + 1; j < this._items.length; j++)
							{
								if (idx == this._items[j])
								{
									PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent duplicate refund index", GeneralConfig.DEFAULT_PUNISH);
									return;
								}
							}

							Item item = refund[idx];
							ItemTemplate template = item.getTemplate();
							objectIds[i] = item.getObjectId();

							for (int jx = 0; jx < i; jx++)
							{
								if (objectIds[i] == objectIds[jx])
								{
									PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " has duplicate items in refund list", GeneralConfig.DEFAULT_PUNISH);
									return;
								}
							}

							long count = item.getCount();
							weight += count * template.getWeight();
							adena += count * (template.getReferencePrice() / 2);
							if (!template.isStackable())
							{
								slots += count;
							}
							else if (player.getInventory().getItemByItemId(template.getId()) == null)
							{
								slots++;
							}
						}

						if (weight > 2147483647L || weight < 0L || !player.getInventory().validateWeight((int) weight))
						{
							player.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (slots > 2147483647L || slots < 0L || !player.getInventory().validateCapacity((int) slots))
						{
							player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (MerchantZeroSellPriceConfig.MERCHANT_ZERO_SELL_PRICE || adena >= 0L && player.reduceAdena(ItemProcessType.FEE, adena, player.getLastFolkNPC(), false))
						{
							for (int i = 0; i < this._items.length; i++)
							{
								Item item = player.getRefund().transferItem(ItemProcessType.TRANSFER, objectIds[i], Long.MAX_VALUE, player.getInventory(), player, player.getLastFolkNPC());
								if (item == null)
								{
									PacketLogger.warning("Error refunding object for char " + player.getName() + " (newitem == null)");
								}
							}

							player.sendPacket(new ExUserInfoInvenWeight(player));
							player.sendPacket(new ExBuySellList(player, true));
						}
						else
						{
							player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
					}
				}
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
