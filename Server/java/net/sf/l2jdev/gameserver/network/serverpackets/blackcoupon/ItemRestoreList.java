package net.sf.l2jdev.gameserver.network.serverpackets.blackcoupon;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.item.enums.BlackCouponRestoreCategory;
import net.sf.l2jdev.gameserver.model.item.holders.ItemRestoreHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ItemRestoreList extends ServerPacket
{
	private final BlackCouponRestoreCategory _category;
	private final List<ItemRestoreHolder> _restoreItems;

	public ItemRestoreList()
	{
		this._category = BlackCouponRestoreCategory.WEAPON;
		this._restoreItems = BlackCouponManager.getInstance().getRestoreItems(0, this._category);
	}

	public ItemRestoreList(int playerObjectId)
	{
		this._category = BlackCouponRestoreCategory.WEAPON;
		this._restoreItems = BlackCouponManager.getInstance().getRestoreItems(playerObjectId, this._category);
	}

	public ItemRestoreList(int playerObjectId, BlackCouponRestoreCategory category)
	{
		this._category = category;
		this._restoreItems = BlackCouponManager.getInstance().getRestoreItems(playerObjectId, this._category);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_RESTORE_LIST.writeId(this, buffer);
		buffer.writeByte(this._category.ordinal());
		buffer.writeInt(this._restoreItems.size());

		for (ItemRestoreHolder holder : this._restoreItems)
		{
			buffer.writeInt(holder.getDestroyedItemId());
			buffer.writeInt(holder.getRepairItemId());
			buffer.writeByte(holder.getEnchantLevel());
			buffer.writeByte(this._restoreItems.lastIndexOf(holder));
		}
	}
}
