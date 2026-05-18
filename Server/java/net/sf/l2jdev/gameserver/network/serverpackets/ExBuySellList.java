package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.custom.MerchantZeroSellPriceConfig;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.buylist.Product;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.TaxType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBuySellList extends AbstractItemPacket
{
	public static final int BUY_SELL_LIST_BUY = 0;
	public static final int BUY_SELL_LIST_SELL = 1;
	public static final int BUY_SELL_LIST_UNK = 2;
	public static final int BUY_SELL_LIST_TAX = 3;
	public static final int UNK_SELECT_FIRST_TAB = 0;
	public static final int UNK_SHOW_PURCHASE_LIST = 1;
	public static final int UNK_SEND_NOT_ENOUGH_ADENA_MESSAGE = 2;
	public static final int UNK_SEND_INCORRECT_ITEM_MESSAGE = 3;
	private static final int[] CASTLES = new int[]
	{
		3,
		7,
		5
	};
	private final int _inventorySlots;
	private final int _type;
	private long _money;
	private double _castleTaxRate;
	private Collection<Product> _list;
	private int _listId;
	private final List<Item> _sellList = new ArrayList<>();
	private final Collection<Item> _refundList = new ArrayList<>();
	private boolean _done;
	private int _unkType;

	public ExBuySellList(ProductList list, Player player, double castleTaxRate)
	{
		this._type = 0;
		this._listId = list.getListId();
		this._list = list.getProducts();
		this._money = player.isGM() && player.getAdena() == 0L && list.getNpcsAllowed() == null ? 1000000000L : player.getAdena();
		this._inventorySlots = player.getInventory().getNonQuestSize();
		this._castleTaxRate = castleTaxRate;
	}

	public ExBuySellList(Player player, boolean done)
	{
		this._type = 1;
		Summon pet = player.getPet();

		for (Item item : player.getInventory().getItems())
		{
			if (!item.isEquipped() && item.isSellable() && (pet == null || item.getObjectId() != pet.getControlObjectId()))
			{
				this._sellList.add(item);
			}
		}

		this._inventorySlots = player.getInventory().getNonQuestSize();
		if (player.hasRefund())
		{
			this._refundList.addAll(player.getRefund().getItems());
		}

		this._done = done;
	}

	public ExBuySellList(int type)
	{
		this._type = 2;
		this._unkType = type;
		this._inventorySlots = 0;
	}

	public ExBuySellList()
	{
		this._type = 3;
		this._inventorySlots = 0;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BUY_SELL_LIST.writeId(this, buffer);
		buffer.writeInt(this._type);
		switch (this._type)
		{
			case 0:
				this.sendBuyList(buffer);
				break;
			case 1:
				this.sendSellList(buffer);
				break;
			case 2:
				this.sendUnk(buffer);
				break;
			case 3:
				this.sendCurrentTax(buffer);
				break;
			default:
				PacketLogger.warning(this.getClass().getSimpleName() + ": unknown type " + this._type);
		}
	}

	private void sendBuyList(WritableBuffer buffer)
	{
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

	private void sendSellList(WritableBuffer buffer)
	{
		buffer.writeInt(this._inventorySlots);
		if (!this._sellList.isEmpty())
		{
			buffer.writeShort(this._sellList.size());

			for (Item item : this._sellList)
			{
				this.writeItem(item, buffer);
				buffer.writeLong(MerchantZeroSellPriceConfig.MERCHANT_ZERO_SELL_PRICE ? 0L : item.getTemplate().getReferencePrice() / 2);
			}
		}
		else
		{
			buffer.writeShort(0);
		}

		if (!this._refundList.isEmpty())
		{
			buffer.writeShort(this._refundList.size());
			int i = 0;

			for (Item item : this._refundList)
			{
				this.writeItem(item, buffer);
				buffer.writeInt(i++);
				buffer.writeLong(MerchantZeroSellPriceConfig.MERCHANT_ZERO_SELL_PRICE ? 0L : item.getTemplate().getReferencePrice() / 2 * item.getCount());
			}
		}
		else
		{
			buffer.writeShort(0);
		}

		buffer.writeByte(this._done ? 1 : 0);
	}

	private void sendUnk(WritableBuffer buffer)
	{
		buffer.writeByte(this._unkType);
	}

	protected void sendCurrentTax(WritableBuffer buffer)
	{
		buffer.writeInt(CASTLES.length);

		for (int id : CASTLES)
		{
			buffer.writeInt(id);

			try
			{
				buffer.writeInt(CastleManager.getInstance().getCastleById(id).getTaxPercent(TaxType.BUY));
			}
			catch (NullPointerException var7)
			{
				buffer.writeInt(0);
			}
		}
	}
}
