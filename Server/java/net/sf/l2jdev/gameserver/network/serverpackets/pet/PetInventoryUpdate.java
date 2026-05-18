package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractInventoryUpdate;

public class PetInventoryUpdate extends AbstractInventoryUpdate
{
	public PetInventoryUpdate()
	{
	}

	public PetInventoryUpdate(Item item)
	{
		super(item);
	}

	public PetInventoryUpdate(List<ItemInfo> items)
	{
		super(items);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PET_INVENTORY_UPDATE.writeId(this, buffer);
		this.writeItems(buffer);
	}
}
