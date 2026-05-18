package net.sf.l2jdev.loginserver.network.clientpackets;

import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.network.LoginClient;
import net.sf.l2jdev.loginserver.network.serverpackets.ServerList;

public class RequestServerList extends LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	protected int _data3;
	
	@Override
	protected boolean readImpl()
	{
		if (this.remaining() >= 8)
		{
			this._skey1 = this.readInt();
			this._skey2 = this.readInt();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		LoginClient client = this.getClient();
		if (client.getSessionKey().checkLoginPair(this._skey1, this._skey2))
		{
			client.sendPacket(new ServerList(client));
		}
		else
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
