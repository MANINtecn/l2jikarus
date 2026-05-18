package net.sf.l2jdev.gameserver.network.clientpackets.autopeel;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.autopeel.ExReadyItemAutoPeel;
import net.sf.l2jdev.gameserver.network.serverpackets.autopeel.ExStopItemAutoPeel;

public class ExRequestStopItemAutoPeel extends ClientPacket
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
			player.removeRequest(AutoPeelRequest.class);
			player.sendPacket(new ExStopItemAutoPeel(true));
			player.sendPacket(new ExReadyItemAutoPeel(false, 0));
		}
	}
}
