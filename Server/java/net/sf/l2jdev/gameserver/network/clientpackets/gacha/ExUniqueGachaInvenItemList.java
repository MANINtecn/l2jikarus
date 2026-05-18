package net.sf.l2jdev.gameserver.network.clientpackets.gacha;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.managers.events.UniqueGachaManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.gacha.UniqueGachaInvenItemList;

public class ExUniqueGachaInvenItemList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			List<Item> items = new ArrayList<>(UniqueGachaManager.getInstance().getTemporaryWarehouse(player));
			int totalSize = items.size();
			 
			int totalPages = totalSize / 150;

			for (int i = 0; i <= totalPages; i++)
			{
				player.sendPacket(new UniqueGachaInvenItemList(i + 1, totalPages + 1, items.subList(i * 150, Math.min((i + 1) * 150, totalSize))));
			}
		}
	}
}
