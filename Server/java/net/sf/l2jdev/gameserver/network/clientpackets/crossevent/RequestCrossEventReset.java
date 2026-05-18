package net.sf.l2jdev.gameserver.network.clientpackets.crossevent;

import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventReset;

public class RequestCrossEventReset extends ClientPacket
{
 

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int resetCount = player.getPlayerResetCount();
			if (resetCount >= 1)
			{
				player.sendPacket(new ExCrossEventReset());
				player.setPlayerResetCount(resetCount - 1);
				player.getCrossEventCells().clear();
				CrossEventManager.getInstance().resetAdvancedRewards(player);
				player.sendPacket(new ExCrossEventInfo(player));
				player.getInventory().destroyItemByItemId(ItemProcessType.FEE, 57, CrossEventManager.getInstance().getResetCostAmount(), player, null);
			}
		}
	}
}
