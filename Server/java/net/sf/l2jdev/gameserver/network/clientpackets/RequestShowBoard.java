package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.handler.CommunityBoardHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestShowBoard extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CommunityBoardHandler.getInstance().handleParseCommand(GeneralConfig.BBS_DEFAULT, player);
		}
	}
}
