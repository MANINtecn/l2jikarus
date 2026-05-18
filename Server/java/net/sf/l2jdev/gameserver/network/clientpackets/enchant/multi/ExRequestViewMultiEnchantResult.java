package net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutInit;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi.ExResultMultiEnchantItemList;

public class ExRequestViewMultiEnchantResult extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request != null)
			{
				player.sendPacket(new ExResultMultiEnchantItemList(player, request.getMultiSuccessEnchantList(), request.getMultiFailureEnchantList(), true));
				player.sendPacket(new ShortcutInit(player));
			}
		}
	}
}
