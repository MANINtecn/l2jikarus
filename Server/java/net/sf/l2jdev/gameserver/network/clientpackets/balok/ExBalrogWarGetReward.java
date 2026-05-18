package net.sf.l2jdev.gameserver.network.clientpackets.balok;

import net.sf.l2jdev.gameserver.managers.BattleWithBalokManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.balok.BalrogWarGetReward;

public class ExBalrogWarGetReward extends ClientPacket
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
			int availableReward = player.getVariables().getInt("BALOK_AVAILABLE_REWARD", 0);
			if (availableReward == 1)
			{
				int count = 1;
				int globalStage = BattleWithBalokManager.getInstance().getGlobalStage();
				if (globalStage < 4)
				{
					count = 30;
				}

				int reward = BattleWithBalokManager.getInstance().getReward();
				player.addItem(ItemProcessType.REWARD, reward, count, player, true);
				player.getVariables().set("BALOK_AVAILABLE_REWARD", -1);
				player.sendPacket(new BalrogWarGetReward(true));
			}
		}
	}
}
