package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.BuyListData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Merchant;
import net.sf.l2jdev.gameserver.model.buylist.Product;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.siege.TaxType;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBuySellList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoInvenWeight;

public class RequestBuyItem extends ClientPacket
{
 
	private int _listId;
	private List<ItemHolder> _items = null;

	@Override
	protected void readImpl()
	{
		this._listId = this.readInt();
		int size = this.readInt();
		if (size > 0 && size <= PlayerConfig.MAX_ITEM_IN_PACKET && size * 12 == this.remaining())
		{
			this._items = new ArrayList<>(size);

			for (int i = 0; i < size; i++)
			{
				int itemId = this.readInt();
				long count = this.readLong();
				if (itemId < 1 || count < 1L)
				{
					this._items = null;
					return;
				}

				this._items.add(new ItemHolder(itemId, count));
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
				player.sendMessage("You are buying too fast.");
			}
			else if (this._items == null)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getReputation() < 0)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				WorldObject target = player.getTarget();
				Merchant merchant = null;
				if (!player.isGM() && this._listId != 423)
				{
					if (!(target instanceof Merchant) || !player.isInsideRadius3D(target, 250) || player.getInstanceWorld() != target.getInstanceWorld())
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
					else
					{
						double castleTaxRate = 0.0;
						if (merchant != null)
						{
							if (!buyList.isNpcAllowed(merchant.getId()))
							{
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							castleTaxRate = merchant.getCastleTaxRate(TaxType.BUY);
						}

						long subTotal = 0L;
						long slots = 0L;
						long weight = 0L;

						for (ItemHolder i : this._items)
						{
							Product product = buyList.getProductByItemId(i.getId());
							if (product == null)
							{
								PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + this._listId + " and item_id " + i.getId(), GeneralConfig.DEFAULT_PUNISH);
								return;
							}

							if (!product.getItem().isStackable() && i.getCount() > 1L)
							{
								PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time.", GeneralConfig.DEFAULT_PUNISH);
								player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
								return;
							}

							long price = product.getPrice();
							if (price < 0L)
							{
								PacketLogger.warning("ERROR, no price found .. wrong buylist ??");
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							if (price == 0L && !player.isGM() && GeneralConfig.ONLY_GM_ITEMS_FREE)
							{
								player.sendMessage("Ohh Cheat does not work? You have a problem now!");
								PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried buy item for 0 adena.", GeneralConfig.DEFAULT_PUNISH);
								return;
							}

							if (product.hasLimitedStock() && i.getCount() > product.getCount())
							{
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							if (Inventory.MAX_ADENA / i.getCount() < price)
							{
								PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Inventory.MAX_ADENA + " adena worth of goods.", GeneralConfig.DEFAULT_PUNISH);
								return;
							}

							price = (long) (price * (1.0 + castleTaxRate + product.getBaseTaxRate()));
							subTotal += i.getCount() * price;
							if (subTotal > Inventory.MAX_ADENA)
							{
								PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Inventory.MAX_ADENA + " adena worth of goods.", GeneralConfig.DEFAULT_PUNISH);
								return;
							}

							weight += i.getCount() * product.getItem().getWeight();
							if (player.getInventory().getItemByItemId(product.getItemId()) == null)
							{
								slots++;
							}
						}

						if (!player.isGM() && (weight > 2147483647L || weight < 0L || !player.getInventory().validateWeight((int) weight)))
						{
							player.sendPacket(SystemMessageId.WEIGHT_LIMIT_IS_EXCEEDED);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (!player.isGM() && (slots > 2147483647L || slots < 0L || !player.getInventory().validateCapacity((int) slots)))
						{
							player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (subTotal >= 0L && player.reduceAdena(ItemProcessType.BUY, subTotal, player.getLastFolkNPC(), false))
						{
							for (ItemHolder i : this._items)
							{
								Product productx = buyList.getProductByItemId(i.getId());
								if (productx == null)
								{
									PunishmentManager.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + this._listId + " and item_id " + i.getId(), GeneralConfig.DEFAULT_PUNISH);
								}
								else if (productx.hasLimitedStock())
								{
									if (productx.decreaseCount(i.getCount()))
									{
										player.getInventory().addItem(ItemProcessType.BUY, i.getId(), i.getCount(), player, merchant);
									}
								}
								else
								{
									player.getInventory().addItem(ItemProcessType.BUY, i.getId(), i.getCount(), player, merchant);
								}
							}

							if (merchant != null)
							{
								merchant.handleTaxPayment(subTotal);
							}

							player.sendPacket(new ExUserInfoInvenWeight(player));
							player.sendPacket(new ExBuySellList(player, true));
							player.sendPacket(SystemMessageId.EXCHANGE_IS_SUCCESSFUL);
						}
						else
						{
							player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
					}
				}
			}
		}
	}
}
