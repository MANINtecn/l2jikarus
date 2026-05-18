package net.sf.l2jdev.loginserver.network.clientpackets;

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import net.sf.l2jdev.loginserver.GameServerTable;
import net.sf.l2jdev.loginserver.LoginController;
import net.sf.l2jdev.loginserver.config.LoginConfig;
import net.sf.l2jdev.loginserver.enums.AccountKickedReason;
import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.model.data.AccountInfo;
import net.sf.l2jdev.loginserver.network.ConnectionState;
import net.sf.l2jdev.loginserver.network.LoginClient;
import net.sf.l2jdev.loginserver.network.serverpackets.AccountKicked;
import net.sf.l2jdev.loginserver.network.serverpackets.LoginOk;
import net.sf.l2jdev.loginserver.network.serverpackets.ServerList;

public class RequestAuthLogin extends LoginClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestAuthLogin.class.getName());
	private final byte[] _raw1 = new byte[128];
	private final byte[] _raw2 = new byte[128];
	private boolean _newAuthMethod = false;
	
	@Override
	protected boolean readImpl()
	{
		if (this.remaining() >= 256)
		{
			this._newAuthMethod = true;
			this.readBytes(this._raw1);
			this.readBytes(this._raw2);
			return true;
		}
		else if (this.remaining() >= 128)
		{
			this.readBytes(this._raw1);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public void run()
	{
		if (!LoginConfig.ENABLE_CMD_LINE_LOGIN || !LoginConfig.ONLY_CMD_LINE_LOGIN)
		{
			LoginClient client = this.getClient();
			byte[] decrypted = new byte[this._newAuthMethod ? 256 : 128];
			
			try
			{
				Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
				rsaCipher.init(2, client.getRSAPrivateKey());
				rsaCipher.doFinal(this._raw1, 0, 128, decrypted, 0);
				if (this._newAuthMethod)
				{
					rsaCipher.doFinal(this._raw2, 0, 128, decrypted, 128);
				}
			}
			catch (GeneralSecurityException var10)
			{
				LOGGER.log(Level.INFO, "", var10);
				return;
			}
			
			String password;
			String user;
			try
			{
				if (this._newAuthMethod)
				{
					user = new String(decrypted, 78, 50).trim() + new String(decrypted, 206, 14).trim();
					password = new String(decrypted, 220, 16).trim();
				}
				else
				{
					user = new String(decrypted, 94, 14).trim();
					password = new String(decrypted, 108, 16).trim();
				}
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.WARNING, "", var9);
				return;
			}
			
			String clientAddr = client.getIp();
			LoginController lc = LoginController.getInstance();
			AccountInfo info = lc.retriveAccountInfo(clientAddr, user, password);
			if (info == null)
			{
				client.close(LoginFailReason.REASON_ACCESS_FAILED);
			}
			else
			{
				switch (lc.tryCheckinAccount(client, clientAddr, info))
				{
					case AUTH_SUCCESS:
						client.setAccount(info.getLogin());
						client.setConnectionState(ConnectionState.AUTHED_LOGIN);
						client.setSessionKey(lc.assignSessionKeyToClient(info.getLogin(), client));
						lc.getCharactersOnAccount(info.getLogin());
						if (LoginConfig.SHOW_LICENCE)
						{
							client.sendPacket(new LoginOk(client.getSessionKey()));
						}
						else
						{
							client.sendPacket(new ServerList(client));
						}
						break;
					case INVALID_PASSWORD:
						client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
						break;
					case ACCOUNT_BANNED:
						client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
						return;
					case ALREADY_ON_LS:
						LoginClient oldClient = lc.getAuthedClient(info.getLogin());
						if (oldClient != null)
						{
							oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
							lc.removeAuthedLoginClient(info.getLogin());
						}
						
						client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
						break;
					case ALREADY_ON_GS:
						GameServerTable.GameServerInfo gsi = lc.getAccountOnGameServer(info.getLogin());
						if (gsi != null)
						{
							client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
							if (gsi.isAuthed())
							{
								gsi.getGameServerThread().kickPlayer(info.getLogin());
							}
						}
				}
			}
		}
	}
}
