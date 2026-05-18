package net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi;

import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.multi.ExResetSelectMultiEnchantScroll;

public class ExRequestStartMultiEnchantScroll extends ClientPacket
{
	private int _scrollObjectId;

	@Override
	protected void readImpl()
	{
		this._scrollObjectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.getRequest(EnchantItemRequest.class) == null)
			{
				player.addRequest(new EnchantItemRequest(player, this._scrollObjectId));
			}

			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			Item scroll = player.getInventory().getItemByObjectId(this._scrollObjectId);
			EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
			if (scrollTemplate != null && !scrollTemplate.isBlessed() && !scrollTemplate.isBlessedDown() && !scrollTemplate.isSafe() && !scrollTemplate.isGiant())
			{
				request.setEnchantingScroll(this._scrollObjectId);
				player.sendPacket(new ExResetSelectMultiEnchantScroll(player, this._scrollObjectId, 0));
			}
			else
			{
				player.sendPacket(new ExResetSelectMultiEnchantScroll(player, this._scrollObjectId, 1));
			}
		}
	}
}
