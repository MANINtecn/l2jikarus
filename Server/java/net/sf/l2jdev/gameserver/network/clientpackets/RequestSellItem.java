package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.custom.MerchantZeroSellPriceConfig;
import net.sf.l2jdev.gameserver.data.xml.BuyListData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Merchant;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.UniqueItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBuySellList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoInvenWeight;

public class RequestSellItem extends ClientPacket
{
 
	private int _listId;
	private List<UniqueItemHolder> _items = null;

	@Override
	protected void readImpl()
	{
		this._listId = this.readInt();
		int size = this.readInt();
		if (size > 0 && size <= PlayerConfig.MAX_ITEM_IN_PACKET && size * 16 == this.remaining())
		{
			this._items = new ArrayList<>(size);

			for (int i = 0; i < size; i++)
			{
				int objectId = this.readInt();
				int itemId = this.readInt();
				long count = this.readLong();
				if (objectId < 1 || itemId < 1 || count < 1L)
				{
					this._items = null;
					return;
				}

				this._items.add(new UniqueItemHolder(itemId, objectId, count));
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
					if (target == null || !player.isInsideRadius3D(target, 250) || player.getInstanceId() != target.getInstanceId() || !(target instanceof Merchant))
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
						long totalPrice = 0L;

						for (UniqueItemHolder i : this._items)
						{
							Item item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "sell");
							if (item != null && item.isSellable())
							{
								long price = item.getReferencePrice() / 2L;
								totalPrice += price * i.getCount();
								if (Inventory.MAX_ADENA / i.getCount() < price || totalPrice > Inventory.MAX_ADENA)
								{
									player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_YOUR_OUT_OF_POCKET_ADENA_LIMIT);
									return;
								}

								if (GeneralConfig.ALLOW_REFUND)
								{
									player.getInventory().transferItem(ItemProcessType.TRANSFER, i.getObjectId(), i.getCount(), player.getRefund(), player, merchant);
								}
								else
								{
									player.getInventory().destroyItem(ItemProcessType.SELL, i.getObjectId(), i.getCount(), player, merchant);
								}
							}
						}

						if (!MerchantZeroSellPriceConfig.MERCHANT_ZERO_SELL_PRICE)
						{
							player.addAdena(ItemProcessType.SELL, totalPrice, merchant, false);
						}

						player.sendPacket(new ExUserInfoInvenWeight(player));
						player.sendPacket(new ExBuySellList(player, true));
						player.sendPacket(SystemMessageId.EXCHANGE_IS_SUCCESSFUL);
					}
				}
			}
		}
	}
}
