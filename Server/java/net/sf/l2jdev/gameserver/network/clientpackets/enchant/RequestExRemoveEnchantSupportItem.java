package net.sf.l2jdev.gameserver.network.clientpackets.enchant;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.ExRemoveEnchantSupportItemResult;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ChangedEnchantTargetItemProbabilityList;

public class RequestExRemoveEnchantSupportItem extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request != null && !request.isProcessing())
			{
				Item supportItem = request.getSupportItem();
				if (supportItem == null || supportItem.getCount() >= 0L)
				{
					request.setSupportItem(-1);
				}

				request.setTimestamp(System.currentTimeMillis());
				player.sendPacket(ExRemoveEnchantSupportItemResult.STATIC_PACKET);
				player.sendPacket(new ChangedEnchantTargetItemProbabilityList(player, false));
			}
		}
	}
}
