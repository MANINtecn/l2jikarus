package net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi.ExResultSetMultiEnchantItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ChangedEnchantTargetItemProbabilityList;

public class ExRequestSetMultiEnchantItemList extends ClientPacket
{
	private int _slotId;
	private final Map<Integer, Integer> _itemObjectId = new HashMap<>();

	@Override
	protected void readImpl()
	{
		this._slotId = this.readInt();

		for (int i = 1; this.remaining() != 0; i++)
		{
			this._itemObjectId.put(i, this.readInt());
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request == null)
			{
				player.sendPacket(new ExResultSetMultiEnchantItemList(player, 1));
			}
			else
			{
				if (request.getMultiEnchantingItemsBySlot(this._slotId) != -1)
				{
					request.clearMultiEnchantingItemsBySlot();

					for (int i = 1; i <= this._slotId; i++)
					{
						request.addMultiEnchantingItems(i, this._itemObjectId.get(i));
					}
				}
				else
				{
					request.addMultiEnchantingItems(this._slotId, this._itemObjectId.get(this._slotId));
				}

				this._itemObjectId.clear();
				player.sendPacket(new ExResultSetMultiEnchantItemList(player, 0));
				player.sendPacket(new ChangedEnchantTargetItemProbabilityList(player, true));
			}
		}
	}
}
