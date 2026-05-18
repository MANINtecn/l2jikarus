package net.sf.l2jdev.gameserver.network.clientpackets.randomcraft;

import net.sf.l2jdev.gameserver.config.RandomCraftConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerRandomCraft;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestRandomCraftRefresh extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		if (RandomCraftConfig.ENABLE_RANDOM_CRAFT)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				PlayerRandomCraft rc = player.getRandomCraft();
				rc.refresh();
			}
		}
	}
}
