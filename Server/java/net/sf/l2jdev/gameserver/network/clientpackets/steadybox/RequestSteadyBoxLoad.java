package net.sf.l2jdev.gameserver.network.clientpackets.steadybox;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.achievementbox.ExSteadyAllBoxUpdate;

public class RequestSteadyBoxLoad extends ClientPacket
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
			player.getAchievementBox().tryFinishBox();
			player.sendPacket(new ExSteadyAllBoxUpdate(player));
		}
	}
}
