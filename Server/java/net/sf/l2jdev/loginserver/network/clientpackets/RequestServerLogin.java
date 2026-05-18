package net.sf.l2jdev.loginserver.network.clientpackets;

import net.sf.l2jdev.loginserver.LoginController;
import net.sf.l2jdev.loginserver.LoginServer;
import net.sf.l2jdev.loginserver.SessionKey;
import net.sf.l2jdev.loginserver.config.LoginConfig;
import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.enums.PlayFailReason;
import net.sf.l2jdev.loginserver.network.LoginClient;
import net.sf.l2jdev.loginserver.network.serverpackets.PlayOk;

public class RequestServerLogin extends LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;
	
	@Override
	protected boolean readImpl()
	{
		if (this.remaining() >= 9)
		{
			this._skey1 = this.readInt();
			this._skey2 = this.readInt();
			this._serverId = this.readByte();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		LoginClient client = this.getClient();
		SessionKey sk = client.getSessionKey();
		if (LoginConfig.SHOW_LICENCE && !sk.checkLoginPair(this._skey1, this._skey2))
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
		}
		else if (LoginServer.getInstance().getStatus() != 4 && (LoginServer.getInstance().getStatus() != 5 || client.getAccessLevel() >= 1))
		{
			if (LoginController.getInstance().isLoginPossible(client, this._serverId))
			{
				client.setJoinedGS(true);
				client.sendPacket(new PlayOk(sk));
			}
			else
			{
				client.close(PlayFailReason.REASON_SERVER_OVERLOADED);
			}
		}
		else
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
