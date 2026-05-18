package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestResetNickname extends ClientPacket
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
			player.getAppearance().setTitleColor(16777079);
			player.setTitle("");
			player.broadcastTitleInfo();
		}
	}
}
