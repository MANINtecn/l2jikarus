package net.sf.l2jdev.gameserver.network.serverpackets.limitshop;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.LimitShopProductHolder;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPurchaseLimitCraftItemList extends ServerPacket
{
	private final Player _player;
	private final int _page;
	private final int _totalPages;
	private final Collection<LimitShopProductHolder> _products;

	public ExPurchaseLimitCraftItemList(Player player, int page, int totalPages, Collection<LimitShopProductHolder> products)
	{
		this._player = player;
		this._page = page;
		this._totalPages = totalPages;
		this._products = products;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PURCHASE_LIMIT_CRAFT_ITEM_LIST.writeId(this, buffer);
		buffer.writeByte(this._page);
		buffer.writeByte(this._totalPages);
		buffer.writeInt(this._products.size());

		for (LimitShopProductHolder product : this._products)
		{
			buffer.writeInt(product.getId());
			buffer.writeInt(product.getProductionId());
			if (product.getAccountDailyLimit() > 0)
			{
				if (this._player.getAccountVariables().getInt("LCSDailyCount" + product.getProductionId(), 0) >= product.getAccountDailyLimit())
				{
					buffer.writeInt(0);
				}
				else
				{
					buffer.writeInt(product.getAccountDailyLimit() - this._player.getAccountVariables().getInt("LCSDailyCount" + product.getProductionId(), 0));
				}
			}
			else if (product.getAccountWeeklyLimit() > 0)
			{
				if (this._player.getAccountVariables().getInt("LCSWeeklyCount" + product.getProductionId(), 0) >= product.getAccountWeeklyLimit())
				{
					buffer.writeInt(0);
				}
				else
				{
					buffer.writeInt(product.getAccountWeeklyLimit() - this._player.getAccountVariables().getInt("LCSWeeklyCount" + product.getProductionId(), 0));
				}
			}
			else if (product.getAccountMonthlyLimit() > 0)
			{
				if (this._player.getAccountVariables().getInt("LCSMonthlyCount" + product.getProductionId(), 0) >= product.getAccountMonthlyLimit())
				{
					buffer.writeInt(0);
				}
				else
				{
					buffer.writeInt(product.getAccountMonthlyLimit() - this._player.getAccountVariables().getInt("LCSMonthlyCount" + product.getProductionId(), 0));
				}
			}
			else if (product.getAccountBuyLimit() > 0)
			{
				if (this._player.getAccountVariables().getInt("LCSCount" + product.getProductionId(), 0) >= product.getAccountBuyLimit())
				{
					buffer.writeInt(0);
				}
				else
				{
					buffer.writeInt(product.getAccountBuyLimit() - this._player.getAccountVariables().getInt("LCSCount" + product.getProductionId(), 0));
				}
			}
			else
			{
				buffer.writeInt(1);
			}

			buffer.writeInt(0);
			buffer.writeInt(0);
			float chance1 = product.getChance();
			float chance2value = product.getChance2();
			float chance3value = product.getChance3();
			float chance4value = product.getChance4();
			float chance2 = chance2value == 100.0F ? 100.0F - chance1 : chance2value;
			float sum2 = chance1 + chance2;
			float chance3 = chance3value == 100.0F ? 100.0F - sum2 : chance3value;
			float sum3 = sum2 + chance3;
			float chance4 = chance4value == 100.0F ? 100.0F - sum3 : chance4value;
			float chance5 = chance4value != 100.0F ? 100.0F - (sum3 + chance4) : 0.0F;
			buffer.writeInt((int) (chance1 * 100000.0F));
			buffer.writeInt((int) (chance2 * 100000.0F));
			buffer.writeInt((int) (chance3 * 100000.0F));
			buffer.writeInt((int) (chance4 * 100000.0F));
			buffer.writeInt((int) (chance5 * 100000.0F));
			buffer.writeInt(0);
		}
	}
}
