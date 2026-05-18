package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class InventoryUpdate extends AbstractInventoryUpdate
{
	public InventoryUpdate()
	{
	}

	public InventoryUpdate(Item item)
	{
		super(item);
	}

	public InventoryUpdate(List<ItemInfo> items)
	{
		super(items);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.INVENTORY_UPDATE.writeId(this, buffer);
		this.writeItems(buffer);
	}
}
