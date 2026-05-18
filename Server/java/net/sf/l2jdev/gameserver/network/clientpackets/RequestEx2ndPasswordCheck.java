package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.SecondaryAuthData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.Ex2ndPasswordCheck;

public class RequestEx2ndPasswordCheck extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		if (SecondaryAuthData.getInstance().isEnabled() && !client.getSecondaryAuth().isAuthed())
		{
			client.getSecondaryAuth().openDialog();
		}
		else
		{
			client.sendPacket(new Ex2ndPasswordCheck(2));
		}
	}
}
