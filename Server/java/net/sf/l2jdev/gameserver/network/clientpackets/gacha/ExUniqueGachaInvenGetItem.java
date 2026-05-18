package net.sf.l2jdev.gameserver.network.clientpackets.gacha;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.managers.events.UniqueGachaManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.gacha.UniqueGachaInvenGetItem;

public class ExUniqueGachaInvenGetItem extends ClientPacket
{
	private List<ItemHolder> _requestedItems;

	@Override
	protected void readImpl()
	{
		int size = this.readInt();
		this._requestedItems = new ArrayList<>(size);

		for (int index = 0; index < size; index++)
		{
			this._requestedItems.add(new ItemHolder(this.readInt(), this.readLong()));
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			boolean isSuccess = UniqueGachaManager.getInstance().receiveItemsFromTemporaryWarehouse(player, this._requestedItems);
			player.sendPacket(new UniqueGachaInvenGetItem(isSuccess));
		}
	}
}
