package net.sf.l2jdev.gameserver.network.clientpackets.balok;

import net.sf.l2jdev.gameserver.managers.BattleWithBalokManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.balok.BalrogWarShowRanking;

public class ExBalrogWarShowRanking extends ClientPacket
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
			if (BattleWithBalokManager.getInstance().getInBattle())
			{
				player.sendPacket(new BalrogWarShowRanking());
			}
		}
	}
}
