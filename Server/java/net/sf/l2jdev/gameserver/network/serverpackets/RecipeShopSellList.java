package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ManufactureItem;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class RecipeShopSellList extends ServerPacket
{
	private final Player _buyer;
	private final Player _manufacturer;
	private final double _craftRate;
	private final double _craftCritical;

	public RecipeShopSellList(Player buyer, Player manufacturer)
	{
		this._buyer = buyer;
		this._manufacturer = manufacturer;
		this._craftRate = this._manufacturer.getStat().getValue(Stat.CRAFT_RATE, 0.0);
		this._craftCritical = this._manufacturer.getStat().getValue(Stat.CRAFTING_CRITICAL, 0.0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.RECIPE_SHOP_SELL_LIST.writeId(this, buffer);
		buffer.writeInt(this._manufacturer.getObjectId());
		buffer.writeInt((int) this._manufacturer.getCurrentMp());
		buffer.writeInt(this._manufacturer.getMaxMp());
		buffer.writeLong(this._buyer.getAdena());
		if (!this._manufacturer.hasManufactureShop())
		{
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(this._manufacturer.getManufactureItems().size());

			for (ManufactureItem item : this._manufacturer.getManufactureItems().values())
			{
				buffer.writeInt(item.getRecipeId());
				buffer.writeInt(0);
				buffer.writeLong(item.getCost());
				buffer.writeDouble(Math.min(this._craftRate, 100.0));
				buffer.writeByte(this._craftCritical > 0.0);
				buffer.writeDouble(Math.min(this._craftCritical, 100.0));
			}
		}
	}
}
