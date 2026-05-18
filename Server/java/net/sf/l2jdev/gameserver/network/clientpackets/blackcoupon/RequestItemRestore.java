package net.sf.l2jdev.gameserver.network.clientpackets.blackcoupon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.BlackCouponRestoreCategory;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemRestoreHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.blackcoupon.ItemRestoreResult;

public class RequestItemRestore extends ClientPacket
{
	private int _brokenItemId;
	private short _enchantLevel;

	@Override
	public void readImpl()
	{
		this._brokenItemId = this.readInt();
		this._enchantLevel = this.readByte();
	}

	@Override
	public void runImpl()
	{
		Player player = this.getClient().getPlayer();
		if (player != null)
		{
			BlackCouponManager manager = BlackCouponManager.getInstance();
			if (player.getInventory().getInventoryItemCount(manager.getBlackCouponId(), -1) < 1L)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(ItemRestoreResult.FAIL);
			}
			else
			{
				BlackCouponRestoreCategory category = manager.getCategoryByItemId(this._brokenItemId);
				List<ItemRestoreHolder> holders = manager.getRestoreItems(player.getObjectId(), category);
				List<ItemRestoreHolder> filter = new ArrayList<>();
				holders.stream().filter(destroyedItemHolder -> destroyedItemHolder.getDestroyedItemId() == this._brokenItemId).forEach(filter::add);
				if (filter.isEmpty())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendPacket(ItemRestoreResult.FAIL);
				}
				else
				{
					ItemRestoreHolder restoreHolder = filter.stream().filter(holder -> holder.getEnchantLevel() == this._enchantLevel).min(Comparator.comparing(ItemRestoreHolder::getDestroyDate)).orElse(null);
					if (restoreHolder != null)
					{
						player.destroyItemByItemId(ItemProcessType.FEE, manager.getBlackCouponId(), manager.getBlackCouponCount(), player, true);
						Item item = player.addItem(ItemProcessType.REWARD, restoreHolder.getRepairItemId(), manager.getBlackCouponCount(), restoreHolder.getEnchantLevel(), player, true);
						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						player.sendInventoryUpdate(iu);
						manager.addToDelete(category, restoreHolder);
						player.sendPacket(ItemRestoreResult.SUCCESS);
					}
				}
			}
		}
	}
}
