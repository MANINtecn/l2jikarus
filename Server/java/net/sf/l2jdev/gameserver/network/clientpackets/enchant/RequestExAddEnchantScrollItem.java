package net.sf.l2jdev.gameserver.network.clientpackets.enchant;

import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.ExPutEnchantScrollItemResult;

public class RequestExAddEnchantScrollItem extends ClientPacket
{
	private int _scrollObjectId;

	@Override
	protected void readImpl()
	{
		this._scrollObjectId = this.readInt();
		this.readInt();
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
			if (request == null)
			{
				player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
				player.sendPacket(new ExPutEnchantScrollItemResult(0));
			}
			else
			{
				request.setEnchantingScroll(this._scrollObjectId);
				Item scroll = request.getEnchantingScroll();
				if (scroll == null)
				{
					player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
					player.sendPacket(new ExPutEnchantScrollItemResult(0));
					request.setEnchantingItem(-1);
					request.setEnchantingScroll(-1);
				}
				else
				{
					EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
					if (scrollTemplate == null)
					{
						player.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
						player.sendPacket(new ExPutEnchantScrollItemResult(0));
						request.setEnchantingScroll(-1);
					}
					else
					{
						request.setTimestamp(System.currentTimeMillis());
						player.sendPacket(new ExPutEnchantScrollItemResult(this._scrollObjectId));
					}
				}
			}
		}
	}
}
