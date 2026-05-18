package net.sf.l2jdev.gameserver.network.clientpackets.worldexchange;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeTotalList;

public class ExWorldExchangeTotalList extends ClientPacket
{
	private final List<Integer> itemIds = new LinkedList<>();

	@Override
	protected void readImpl()
	{
		int size = this.readInt();

		for (int index = 0; index < size; index++)
		{
			this.itemIds.add(this.readInt());
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new WorldExchangeTotalList(this.itemIds));
		}
	}
}
