package net.sf.l2jdev.loginserver.network;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.commons.network.Buffer;
import net.sf.l2jdev.commons.network.Client;
import net.sf.l2jdev.commons.network.Connection;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.loginserver.LoginController;
import net.sf.l2jdev.loginserver.SessionKey;
import net.sf.l2jdev.loginserver.enums.AccountKickedReason;
import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.enums.PlayFailReason;
import net.sf.l2jdev.loginserver.network.serverpackets.AccountKicked;
import net.sf.l2jdev.loginserver.network.serverpackets.Init;
import net.sf.l2jdev.loginserver.network.serverpackets.LoginFail;
import net.sf.l2jdev.loginserver.network.serverpackets.LoginServerPacket;
import net.sf.l2jdev.loginserver.network.serverpackets.PlayFail;

public class LoginClient extends Client<Connection<LoginClient>>
{
	private final LoginEncryption _encryption;
	private final ScrambledKeyPair _scrambledPair;
	private final byte[] _blowfishKey;
	private String _ip = "N/A";
	private final int _sessionId;
	private final long _connectionStartTime;
	private String _account;
	private int _accessLevel;
	private int _lastServer;
	private SessionKey _sessionKey;
	private boolean _joinedGS;
	private ConnectionState _connectionState = ConnectionState.CONNECTED;
	private Map<Integer, Integer> _charsOnServers;
	private Map<Integer, long[]> _charsToDelete;
	
	public LoginClient(Connection<LoginClient> connection)
	{
		super(connection);
		this._scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		this._blowfishKey = LoginController.getInstance().getBlowfishKey();
		this._ip = connection.getRemoteAddress();
		this._sessionId = Rnd.nextInt();
		this._connectionStartTime = System.currentTimeMillis();
		this._encryption = new LoginEncryption();
		this._encryption.setKey(this._blowfishKey);
		if (LoginController.getInstance().isBannedAddress(this._ip))
		{
			this.close(LoginFailReason.REASON_NOT_AUTHED);
		}
	}
	
	@Override
	public boolean encrypt(Buffer data, int offset, int size)
	{
		try
		{
			return this._encryption.encrypt(data, offset, size);
		}
		catch (IOException var5)
		{
			return false;
		}
	}
	
	@Override
	public boolean decrypt(Buffer data, int offset, int size)
	{
		boolean decrypted;
		try
		{
			decrypted = this._encryption.decrypt(data, offset, size);
		}
		catch (IOException var6)
		{
			this.close();
			return false;
		}
		
		if (!decrypted)
		{
			this.close();
		}
		
		return decrypted;
	}
	
	@Override
	public void onConnected()
	{
		this.sendPacket(new Init(this));
	}
	
	@Override
	public void onDisconnection()
	{
		if (!this._joinedGS)
		{
			LoginController.getInstance().removeAuthedLoginClient(this._account);
			
			try
			{
				Thread.sleep(1000L);
			}
			catch (InterruptedException var2)
			{
			}
		}
	}
	
	public byte[] getBlowfishKey()
	{
		return this._blowfishKey;
	}
	
	public byte[] getScrambledModulus()
	{
		return this._scrambledPair.getScrambledModulus();
	}
	
	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) this._scrambledPair.getPrivateKey();
	}
	
	public String getIp()
	{
		return this._ip;
	}
	
	public int getSessionId()
	{
		return this._sessionId;
	}
	
	public long getConnectionStartTime()
	{
		return this._connectionStartTime;
	}
	
	public String getAccount()
	{
		return this._account;
	}
	
	public void setAccount(String account)
	{
		this._account = account;
	}
	
	public void setAccessLevel(int accessLevel)
	{
		this._accessLevel = accessLevel;
	}
	
	public int getAccessLevel()
	{
		return this._accessLevel;
	}
	
	public void setLastServer(int lastServer)
	{
		this._lastServer = lastServer;
	}
	
	public int getLastServer()
	{
		return this._lastServer;
	}
	
	public boolean hasJoinedGS()
	{
		return this._joinedGS;
	}
	
	public void setJoinedGS(boolean value)
	{
		this._joinedGS = value;
	}
	
	public void setSessionKey(SessionKey sessionKey)
	{
		this._sessionKey = sessionKey;
	}
	
	public SessionKey getSessionKey()
	{
		return this._sessionKey;
	}
	
	public void setCharsOnServ(int servId, int chars)
	{
		if (this._charsOnServers == null)
		{
			this._charsOnServers = new HashMap<>();
		}
		
		this._charsOnServers.put(servId, chars);
	}
	
	public Map<Integer, Integer> getCharsOnServ()
	{
		return this._charsOnServers;
	}
	
	public void serCharsWaitingDelOnServ(int servId, long[] charsToDel)
	{
		if (this._charsToDelete == null)
		{
			this._charsToDelete = new HashMap<>();
		}
		
		this._charsToDelete.put(servId, charsToDel);
	}
	
	public Map<Integer, long[]> getCharsWaitingDelOnServ()
	{
		return this._charsToDelete;
	}
	
	public ConnectionState getConnectionState()
	{
		return this._connectionState;
	}
	
	public void setConnectionState(ConnectionState connectionState)
	{
		this._connectionState = connectionState;
	}
	
	public void sendPacket(LoginServerPacket packet)
	{
		this.writePacket(packet);
	}
	
	public void close(LoginFailReason reason)
	{
		this.sendPacket(new LoginFail(reason));
	}
	
	public void close(PlayFailReason reason)
	{
		this.close(new PlayFail(reason));
	}
	
	public void close(AccountKickedReason reason)
	{
		this.close(new AccountKicked(reason));
	}
	
	@Override
	public String toString()
	{
		String ip = this.getHostAddress();
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" [");
		if (this._account != null)
		{
			sb.append("Account: ");
			sb.append(this._account);
		}
		
		if (ip != null)
		{
			if (this._account != null)
			{
				sb.append(" - ");
			}
			
			sb.append("IP: ");
			sb.append(ip.isEmpty() ? "disconnected" : ip);
		}
		
		sb.append("]");
		return sb.toString();
	}
}
