package net.sf.l2jdev.gameserver.network.clientpackets.limitshop;

import java.util.List;

import net.sf.l2jdev.gameserver.data.holders.LimitShopProductHolder;
import net.sf.l2jdev.gameserver.data.xml.LimitShopClanData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopCraftData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.limitshop.ExPurchaseLimitCraftItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.limitshop.ExPurchaseLimitShopItemListNew;

public class RequestPurchaseLimitShopItemList extends ClientPacket
{
	 
	private int _shopType;

	@Override
	protected void readImpl()
	{
		this._shopType = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			List<LimitShopProductHolder> products;
			switch (this._shopType)
			{
				case 3:
					products = LimitShopData.getInstance().getProducts();
					break;
				case 4:
					products = LimitShopCraftData.getInstance().getProducts();
					break;
				case 100:
					products = LimitShopClanData.getInstance().getProducts();
					break;
				default:
					return;
			}

			int totalPages = products.size() / 350 + (products.size() % 350 == 0 ? 0 : 1);

			for (int page = 0; page < totalPages; page++)
			{
				int start = page * 350;
				int end = Math.min(start + 350, products.size());
				List<LimitShopProductHolder> productList = products.subList(start, end);
				if (this._shopType == 4)
				{
					player.sendPacket(new ExPurchaseLimitCraftItemList(player, page + 1, totalPages, productList));
				}
				else
				{
					player.sendPacket(new ExPurchaseLimitShopItemListNew(player, this._shopType, page + 1, totalPages, productList));
				}
			}
		}
	}
}
