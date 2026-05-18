package net.sf.l2jdev.gameserver.network.clientpackets.randomcraft;

import net.sf.l2jdev.gameserver.config.RandomCraftConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerRandomCraft;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftRandomInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftRandomLockSlot;

public class ExRequestRandomCraftLockSlot extends ClientPacket
{
	private static final int[] LOCK_PRICE = new int[]
	{
		100,
		500,
		1000
	};
	private int _id;

	@Override
	protected void readImpl()
	{
		this._id = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		if (RandomCraftConfig.ENABLE_RANDOM_CRAFT)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (this._id >= 0 && this._id < 5)
				{
					PlayerRandomCraft rc = player.getRandomCraft();
					int lockedItemCount = rc.getLockedSlotCount();
					if (rc.getRewards().size() - 1 >= this._id && lockedItemCount < 3)
					{
						int price = LOCK_PRICE[Math.min(lockedItemCount, 2)];
						Item lcoin = player.getInventory().getItemByItemId(91663);
						if (lcoin != null && lcoin.getCount() >= price)
						{
							player.destroyItem(ItemProcessType.FEE, lcoin, price, player, true);
							rc.getRewards().get(this._id).lock();
							player.sendPacket(new ExCraftRandomLockSlot());
							player.sendPacket(new ExCraftRandomInfo(player));
						}
					}
				}
			}
		}
	}
}
