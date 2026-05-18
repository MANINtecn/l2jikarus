package net.sf.l2jdev.loginserver;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.loginserver.config.LoginConfig;
import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.enums.LoginResult;
import net.sf.l2jdev.loginserver.model.data.AccountInfo;
import net.sf.l2jdev.loginserver.network.LoginClient;
import net.sf.l2jdev.loginserver.network.ScrambledKeyPair;

public class LoginController
{
	protected static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
	public static final int LOGIN_TIMEOUT = 300000;
	protected byte[][] _blowfishKeys;
	protected ScrambledKeyPair[] _keyPairs;
	protected Map<String, LoginClient> _loginServerClients = new ConcurrentHashMap<>();
	private final Map<String, Integer> _failedLoginAttemps = new HashMap<>();
	private final Map<String, Long> _bannedIps = new ConcurrentHashMap<>();
	private static LoginController INSTANCE;
	
	private LoginController() throws GeneralSecurityException
	{
		LOGGER.info("Loading LoginController...");
		this._keyPairs = new ScrambledKeyPair[10];
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		
		for (int i = 0; i < 10; i++)
		{
			this._keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
		}
		
		LOGGER.info("Cached 10 KeyPairs for RSA communication.");
		this.testCipher((RSAPrivateKey) this._keyPairs[0].getPrivateKey());
		this.generateBlowFishKeys();
		ThreadPool.scheduleAtFixedRate(this::purge, 300000L, 300000L);
	}
	
	protected void testCipher(RSAPrivateKey key) throws GeneralSecurityException
	{
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(2, key);
	}
	
	private void generateBlowFishKeys()
	{
		this._blowfishKeys = new byte[20][16];
		
		for (int i = 0; i < 20; i++)
		{
			for (int j = 0; j < this._blowfishKeys[i].length; j++)
			{
				this._blowfishKeys[i][j] = (byte) Rnd.get(0, 255);
			}
		}
		
		LOGGER.info("Stored " + this._blowfishKeys.length + " keys for Blowfish communication.");
	}
	
	public byte[] getBlowfishKey()
	{
		return this._blowfishKeys[(int) (Math.random() * 20.0)];
	}
	
	public SessionKey assignSessionKeyToClient(String account, LoginClient client)
	{
		SessionKey key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
		this._loginServerClients.put(account, client);
		return key;
	}
	
	public void removeAuthedLoginClient(String account)
	{
		if (account != null)
		{
			this._loginServerClients.remove(account);
		}
	}
	
	public LoginClient getAuthedClient(String account)
	{
		return this._loginServerClients.get(account);
	}
	
	public AccountInfo retriveAccountInfo(String clientAddr, String login, String password)
	{
		return this.retriveAccountInfo(clientAddr, login, password, true);
	}
	
	private void recordFailedLoginAttemp(String addr)
	{
		Integer failedLoginAttemps;
		synchronized (this._failedLoginAttemps)
		{
			failedLoginAttemps = this._failedLoginAttemps.get(addr);
			if (failedLoginAttemps == null)
			{
				failedLoginAttemps = 1;
			}
			else
			{
				failedLoginAttemps = failedLoginAttemps + 1;
			}
			
			this._failedLoginAttemps.put(addr, failedLoginAttemps);
		}
		
		if (failedLoginAttemps >= LoginConfig.LOGIN_TRY_BEFORE_BAN)
		{
			this.addBanForAddress(addr, LoginConfig.LOGIN_BLOCK_AFTER_BAN * 1000);
			this.clearFailedLoginAttemps(addr);
			LOGGER.warning("Added banned address " + addr + "! Too many login attempts.");
		}
	}
	
	private void clearFailedLoginAttemps(String clientAddr)
	{
		synchronized (this._failedLoginAttemps)
		{
			this._failedLoginAttemps.remove(clientAddr);
		}
	}
	
	private AccountInfo retriveAccountInfo(String clientAddr, String login, String password, boolean autoCreateIfEnabled)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes(StandardCharsets.UTF_8);
			String hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));
			
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT login, password, IF(? > value OR value IS NULL, accessLevel, -1) AS accessLevel, lastServer FROM accounts LEFT JOIN (account_data) ON (account_data.account_name=accounts.login AND account_data.var=\"ban_temp\") WHERE login=?");)
			{
				ps.setString(1, Long.toString(System.currentTimeMillis()));
				ps.setString(2, login);
				
				try (ResultSet rset = ps.executeQuery())
				{
					if (rset.next())
					{
						AccountInfo info = new AccountInfo(rset.getString("login"), rset.getString("password"), rset.getInt("accessLevel"), rset.getInt("lastServer"));
						if (!info.checkPassHash(hashBase64))
						{
							this.recordFailedLoginAttemp(clientAddr);
							return null;
						}
						
						this.clearFailedLoginAttemps(clientAddr);
						return info;
					}
				}
			}
			
			if (autoCreateIfEnabled && LoginConfig.AUTO_CREATE_ACCOUNTS)
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (login, password, lastactive, accessLevel, lastIP) values (?, ?, ?, ?, ?)");)
				{
					ps.setString(1, login);
					ps.setString(2, hashBase64);
					ps.setLong(3, System.currentTimeMillis());
					ps.setInt(4, 0);
					ps.setString(5, clientAddr);
					ps.execute();
				}
				catch (Exception var20)
				{
					LOGGER.log(Level.WARNING, "Exception while auto creating account for '" + login + "'!", var20);
					return null;
				}
				
				LOGGER.info("Auto created account '" + login + "'.");
				return this.retriveAccountInfo(clientAddr, login, password, false);
			}
			this.recordFailedLoginAttemp(clientAddr);
			return null;
		}
		catch (Exception var24)
		{
			LOGGER.log(Level.WARNING, "Exception while retriving account info for '" + login + "'!", var24);
			return null;
		}
	}
	
	public LoginResult tryCheckinAccount(LoginClient client, String address, AccountInfo info)
	{
		if (info.getAccessLevel() < 0)
		{
			return LoginResult.ACCOUNT_BANNED;
		}
		LoginResult ret = LoginResult.INVALID_PASSWORD;
		if (this.canCheckin(client, address, info))
		{
			ret = LoginResult.ALREADY_ON_GS;
			if (!this.isAccountInAnyGameServer(info.getLogin()))
			{
				ret = LoginResult.ALREADY_ON_LS;
				if (this._loginServerClients.putIfAbsent(info.getLogin(), client) == null)
				{
					ret = LoginResult.AUTH_SUCCESS;
				}
			}
		}
		
		return ret;
	}
	
	public void addBanForAddress(String address, long duration)
	{
		if (duration > 0L)
		{
			this._bannedIps.putIfAbsent(address, System.currentTimeMillis() + duration);
		}
		else
		{
			this._bannedIps.putIfAbsent(address, Long.MAX_VALUE);
		}
	}
	
	public boolean isBannedAddress(String address)
	{
		String[] parts = address.split("\\.");
		Long bi = this._bannedIps.get(address);
		if (bi == null)
		{
			bi = this._bannedIps.get(parts[0] + "." + parts[1] + "." + parts[2] + ".0");
		}
		
		if (bi == null)
		{
			bi = this._bannedIps.get(parts[0] + "." + parts[1] + ".0.0");
		}
		
		if (bi == null)
		{
			bi = this._bannedIps.get(parts[0] + ".0.0.0");
		}
		
		if (bi != null)
		{
			if (bi > 0L && bi < System.currentTimeMillis())
			{
				this._bannedIps.remove(address);
				LOGGER.info("Removed expired ip address ban " + address + ".");
				return false;
			}
			return true;
		}
		return false;
	}
	
	public Map<String, Long> getBannedIps()
	{
		return this._bannedIps;
	}
	
	public boolean removeBanForAddress(String address)
	{
		return this._bannedIps.remove(address) != null;
	}
	
	public SessionKey getKeyForAccount(String account)
	{
		LoginClient client = this._loginServerClients.get(account);
		return client != null ? client.getSessionKey() : null;
	}
	
	public boolean isAccountInAnyGameServer(String account)
	{
		for (GameServerTable.GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public GameServerTable.GameServerInfo getAccountOnGameServer(String account)
	{
		for (GameServerTable.GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return gsi;
			}
		}
		
		return null;
	}
	
	public void getCharactersOnAccount(String account)
	{
		for (GameServerTable.GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			if (gsi.isAuthed())
			{
				gsi.getGameServerThread().requestCharacters(account);
			}
		}
	}
	
	public boolean isLoginPossible(LoginClient client, int serverId)
	{
		GameServerTable.GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null && gsi.isAuthed())
		{
			boolean loginOk = gsi.canLogin(client);
			if (loginOk && client.getLastServer() != serverId)
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE accounts SET lastServer = ? WHERE login = ?");)
				{
					ps.setInt(1, serverId);
					ps.setString(2, client.getAccount());
					ps.executeUpdate();
				}
				catch (Exception var13)
				{
					LOGGER.log(Level.WARNING, "Could not set lastServer: " + var13.getMessage(), var13);
				}
			}
			
			return loginOk;
		}
		return false;
	}
	
	public void setAccountAccessLevel(String account, int banLevel)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE accounts SET accessLevel = ? WHERE login = ?");)
		{
			ps.setInt(1, banLevel);
			ps.setString(2, account);
			ps.executeUpdate();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, "Could not set accessLevel: " + var11.getMessage(), var11);
		}
	}
	
	public void setAccountLastTracert(String account, String pcIp, String hop1, String hop2, String hop3, String hop4)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pcIp = ?, hop1 = ?, hop2 = ?, hop3 = ?, hop4 = ? WHERE login = ?");)
		{
			ps.setString(1, pcIp);
			ps.setString(2, hop1);
			ps.setString(3, hop2);
			ps.setString(4, hop3);
			ps.setString(5, hop4);
			ps.setString(6, account);
			ps.executeUpdate();
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not set last tracert: " + var15.getMessage(), var15);
		}
	}
	
	public void setCharactersOnServer(String account, int charsNum, long[] timeToDel, int serverId)
	{
		LoginClient client = this._loginServerClients.get(account);
		if (client != null)
		{
			if (charsNum > 0)
			{
				client.setCharsOnServ(serverId, charsNum);
			}
			
			if (timeToDel.length > 0)
			{
				client.serCharsWaitingDelOnServ(serverId, timeToDel);
			}
		}
	}
	
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return this._keyPairs[Rnd.get(10)];
	}
	
	public boolean canCheckin(LoginClient client, String address, AccountInfo info)
	{
		try
		{
			List<String> ipWhiteList = new ArrayList<>();
			List<String> ipBlackList = new ArrayList<>();
			
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_ipauth WHERE login = ?");)
			{
				ps.setString(1, info.getLogin());
				
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						String ip = rset.getString("ip");
						String type = rset.getString("type");
						if (this.isValidIPAddress(ip))
						{
							if (type.equals("allow"))
							{
								ipWhiteList.add(ip);
							}
							else if (type.equals("deny"))
							{
								ipBlackList.add(ip);
							}
						}
					}
				}
			}
			
			if (!ipWhiteList.isEmpty() || !ipBlackList.isEmpty())
			{
				if (!ipWhiteList.isEmpty() && !ipWhiteList.contains(address))
				{
					LOGGER.warning("Account checkin attemp from address(" + address + ") not present on whitelist for account '" + info.getLogin() + "'.");
					return false;
				}
				
				if (!ipBlackList.isEmpty() && ipBlackList.contains(address))
				{
					LOGGER.warning("Account checkin attemp from address(" + address + ") on blacklist for account '" + info.getLogin() + "'.");
					return false;
				}
			}
			
			client.setAccessLevel(info.getAccessLevel());
			client.setLastServer(info.getLastServer());
			
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE accounts SET lastactive = ?, lastIP = ? WHERE login = ?");)
			{
				ps.setLong(1, System.currentTimeMillis());
				ps.setString(2, address);
				ps.setString(3, info.getLogin());
				ps.execute();
			}
			
			return true;
		}
		catch (Exception var21)
		{
			LOGGER.log(Level.WARNING, "Could not finish login process!", var21);
			return false;
		}
	}
	
	public boolean isValidIPAddress(String ipAddress)
	{
		String[] parts = ipAddress.split("\\.");
		if (parts.length != 4)
		{
			return false;
		}
		for (String s : parts)
		{
			int i = Integer.parseInt(s);
			if (i < 0 || i > 255)
			{
				return false;
			}
		}
		
		return true;
	}
	
	private void purge()
	{
		if (!this._loginServerClients.isEmpty())
		{
			long currentTime = System.currentTimeMillis();
			Iterator<Entry<String, LoginClient>> iterator = this._loginServerClients.entrySet().iterator();
			
			while (iterator.hasNext())
			{
				LoginClient client = iterator.next().getValue();
				if (!client.hasJoinedGS() && client.getConnectionStartTime() + 300000L <= currentTime || !client.isConnected())
				{
					client.close(LoginFailReason.REASON_ACCESS_FAILED);
					iterator.remove();
				}
			}
		}
	}
	
	public static void load() throws GeneralSecurityException
	{
		synchronized (LoginController.class)
		{
			if (INSTANCE == null)
			{
				INSTANCE = new LoginController();
			}
			else
			{
				throw new IllegalStateException("LoginController can only be loaded a single time.");
			}
		}
	}
	
	public static LoginController getInstance()
	{
		return INSTANCE;
	}
}
