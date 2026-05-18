package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.CharSelectionInfo;

public class RequestGotoLobby extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		client.sendPacket(new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
	}
}
