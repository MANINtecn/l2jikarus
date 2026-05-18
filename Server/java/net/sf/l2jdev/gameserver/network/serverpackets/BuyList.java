package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.buylist.Product;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class BuyList extends AbstractItemPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private final int _inventorySlots;
	private final double _castleTaxRate;

	public BuyList(ProductList list, Player player, double castleTaxRate)
	{
		this._listId = list.getListId();
		this._list = list.getProducts();
		this._money = player.getAdena();
		this._inventorySlots = player.getInventory().getNonQuestSize();
		this._castleTaxRate = castleTaxRate;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BUY_SELL_LIST.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeLong(this._money);
		buffer.writeInt(this._listId);
		buffer.writeInt(this._inventorySlots);
		buffer.writeShort(this._list.size());

		for (Product product : this._list)
		{
			if (product.getCount() > 0L || !product.hasLimitedStock())
			{
				this.writeItem(product, buffer);
				buffer.writeLong((long) (product.getPrice() * (1.0 + this._castleTaxRate + product.getBaseTaxRate())));
			}
		}
	}
}
