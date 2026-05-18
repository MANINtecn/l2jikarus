package net.sf.l2jdev.gameserver.network.clientpackets.worldexchange;

import java.util.Collections;

import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeSettleList;

public class ExWorldExchangeSettleList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				player.sendPacket(new WorldExchangeItemList(Collections.emptyList(), null, 0));
				player.sendPacket(new WorldExchangeSettleList(player));
			}
		}
	}
}
