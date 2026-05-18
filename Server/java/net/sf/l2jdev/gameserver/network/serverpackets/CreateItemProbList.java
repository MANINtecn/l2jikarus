package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.ExtractableProduct;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class CreateItemProbList extends ServerPacket
{
	private final int _itemId;
	private final List<ExtractableProduct> _guaranteedItems;
	private final List<ExtractableProduct> _randomItems;

	public CreateItemProbList(int itemId)
	{
		this._itemId = itemId;
		this._guaranteedItems = new ArrayList<>();
		this._randomItems = new ArrayList<>();
		ItemTemplate template = ItemData.getInstance().getTemplate(this._itemId);
		if (template != null && template.isEtcItem())
		{
			List<ExtractableProduct> extractableItems = ((EtcItem) template).getExtractableItems();
			if (extractableItems != null)
			{
				for (ExtractableProduct expi : extractableItems)
				{
					if (expi.getChance() == 100000)
					{
						this._guaranteedItems.add(expi);
					}
					else
					{
						this._randomItems.add(expi);
					}
				}
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CREATE_ITEM_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(this._itemId);
		buffer.writeInt(this._guaranteedItems.size());

		for (ExtractableProduct product : this._guaranteedItems)
		{
			buffer.writeInt(product.getId());
			buffer.writeInt(product.getMinEnchant());
			buffer.writeLong(product.getMin());
			buffer.writeLong(0L);
		}

		buffer.writeInt(this._randomItems.size());

		for (ExtractableProduct product : this._randomItems)
		{
			buffer.writeInt(product.getId());
			buffer.writeInt(product.getMinEnchant());
			buffer.writeLong(product.getMin());
			buffer.writeLong(0L);
		}

		buffer.writeInt(0);
	}
}
