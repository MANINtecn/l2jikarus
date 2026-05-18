package net.sf.l2jdev.loginserver;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.commons.util.Subnet;
import net.sf.l2jdev.loginserver.network.LoginClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class GameServerTable implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(GameServerTable.class.getName());
	private static final Map<Integer, String> SERVER_NAMES = new HashMap<>();
	private static final Map<Integer, GameServerTable.GameServerInfo> GAME_SERVER_TABLE = new HashMap<>();
	private KeyPair[] _keyPairs;
	
	public GameServerTable()
	{
		this.load();
		this.loadRegisteredGameServers();
		LOGGER.info("Loaded " + GAME_SERVER_TABLE.size() + " registered Game Servers.");
		this.initRSAKeys();
		LOGGER.info("Cached " + this._keyPairs.length + " RSA keys for Game Server communication.");
	}
	
	@Override
	public void load()
	{
		SERVER_NAMES.clear();
		this.parseDatapackFile("data/servername.xml");
		LOGGER.info("Loaded " + SERVER_NAMES.size() + " server names.");
	}
	
	@Override
	public void parseDocument(Document document, File file)
	{
		NodeList servers = document.getElementsByTagName("server");
		
		for (int s = 0; s < servers.getLength(); s++)
		{
			SERVER_NAMES.put(this.parseInteger(servers.item(s).getAttributes(), "id"), this.parseString(servers.item(s).getAttributes(), "name"));
		}
	}
	
	private void initRSAKeys()
	{
		try
		{
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4));
			this._keyPairs = new KeyPair[10];
			
			for (int i = 0; i < 10; i++)
			{
				this._keyPairs[i] = keyGen.genKeyPair();
			}
		}
		catch (Exception var3)
		{
			LOGGER.severe("Error loading RSA keys for Game Server communication!");
		}
	}
	
	private void loadRegisteredGameServers()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement ps = con.createStatement(); ResultSet rs = ps.executeQuery("SELECT * FROM gameservers");)
		{
			while (rs.next())
			{
				int id = rs.getInt("server_id");
				GAME_SERVER_TABLE.put(id, new GameServerTable.GameServerInfo(id, this.stringToHex(rs.getString("hexid"))));
			}
		}
		catch (Exception var12)
		{
			LOGGER.severe("Error loading registered game servers!");
		}
	}
	
	public Map<Integer, GameServerTable.GameServerInfo> getRegisteredGameServers()
	{
		return GAME_SERVER_TABLE;
	}
	
	public GameServerTable.GameServerInfo getRegisteredGameServerById(int id)
	{
		return GAME_SERVER_TABLE.get(id);
	}
	
	public boolean hasRegisteredGameServerOnId(int id)
	{
		return GAME_SERVER_TABLE.containsKey(id);
	}
	
	public boolean registerWithFirstAvailableId(GameServerTable.GameServerInfo gsi)
	{
		synchronized (GAME_SERVER_TABLE)
		{
			for (Integer serverId : SERVER_NAMES.keySet())
			{
				if (!GAME_SERVER_TABLE.containsKey(serverId))
				{
					GAME_SERVER_TABLE.put(serverId, gsi);
					gsi.setId(serverId);
					return true;
				}
			}
			
			return false;
		}
	}
	
	public boolean register(int id, GameServerTable.GameServerInfo gsi)
	{
		synchronized (GAME_SERVER_TABLE)
		{
			if (!GAME_SERVER_TABLE.containsKey(id))
			{
				GAME_SERVER_TABLE.put(id, gsi);
				return true;
			}
			return false;
		}
	}
	
	public void registerServerOnDB(GameServerTable.GameServerInfo gsi)
	{
		this.registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
	}
	
	public void registerServerOnDB(byte[] hexId, int id, String externalHost)
	{
		this.register(id, new GameServerTable.GameServerInfo(id, hexId));
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");)
		{
			ps.setString(1, this.hexToString(hexId));
			ps.setInt(2, id);
			ps.setString(3, externalHost);
			ps.executeUpdate();
		}
		catch (Exception var12)
		{
			LOGGER.severe("Error while saving gameserver!");
		}
	}
	
	public String getServerNameById(int id)
	{
		return SERVER_NAMES.get(id);
	}
	
	public Map<Integer, String> getServerNames()
	{
		return SERVER_NAMES;
	}
	
	public KeyPair getKeyPair()
	{
		return this._keyPairs[Rnd.get(10)];
	}
	
	protected byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	protected String hexToString(byte[] hex)
	{
		return hex == null ? "null" : new BigInteger(hex).toString(16);
	}
	
	public static GameServerTable getInstance()
	{
		return GameServerTable.SingletonHolder.INSTANCE;
	}
	
	public static class GameServerInfo
	{
		private int _id;
		private final byte[] _hexId;
		private boolean _isAuthed;
		private GameServerThread _gst;
		private int _status;
		private final List<GameServerTable.GameServerInfo.GameServerAddress> _addrs = new ArrayList<>(5);
		private int _port;
		private int _serverType;
		private int _ageLimit;
		private boolean _isShowingBrackets;
		private int _maxPlayers;
		
		public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
		{
			this._id = id;
			this._hexId = hexId;
			this._gst = gst;
			this._status = 4;
		}
		
		public GameServerInfo(int id, byte[] hexId)
		{
			this(id, hexId, null);
		}
		
		public void setId(int id)
		{
			this._id = id;
		}
		
		public int getId()
		{
			return this._id;
		}
		
		public byte[] getHexId()
		{
			return this._hexId;
		}
		
		public String getName()
		{
			return GameServerTable.getInstance().getServerNameById(this._id);
		}
		
		public void setAuthed(boolean isAuthed)
		{
			this._isAuthed = isAuthed;
		}
		
		public boolean isAuthed()
		{
			return this._isAuthed;
		}
		
		public void setGameServerThread(GameServerThread gst)
		{
			this._gst = gst;
		}
		
		public GameServerThread getGameServerThread()
		{
			return this._gst;
		}
		
		public void setStatus(int status)
		{
			if (LoginServer.getInstance().getStatus() == 4)
			{
				this._status = 4;
			}
			else if (LoginServer.getInstance().getStatus() == 5)
			{
				this._status = 5;
			}
			else
			{
				this._status = status;
			}
		}
		
		public int getStatus()
		{
			return this._status;
		}
		
		public String getStatusName()
		{
			switch (this._status)
			{
				case 0:
					return "Auto";
				case 1:
					return "Good";
				case 2:
					return "Normal";
				case 3:
					return "Full";
				case 4:
					return "Down";
				case 5:
					return "GM Only";
				default:
					return "Unknown";
			}
		}
		
		public int getCurrentPlayerCount()
		{
			return this._gst == null ? 0 : this._gst.getPlayerCount();
		}
		
		public boolean canLogin(LoginClient client)
		{
			if (this._status == 4)
			{
				return false;
			}
			return this._status != 5 && this.getCurrentPlayerCount() < this.getMaxPlayers() ? client.getAccessLevel() >= 0 : client.getAccessLevel() > 0;
		}
		
		public String getExternalHost()
		{
			try
			{
				return this.getServerAddress(InetAddress.getByName("0.0.0.0"));
			}
			catch (Exception var2)
			{
				return null;
			}
		}
		
		public int getPort()
		{
			return this._port;
		}
		
		public void setPort(int port)
		{
			this._port = port;
		}
		
		public void setMaxPlayers(int maxPlayers)
		{
			this._maxPlayers = maxPlayers;
		}
		
		public int getMaxPlayers()
		{
			return this._maxPlayers;
		}
		
		public boolean isPvp()
		{
			return true;
		}
		
		public void setAgeLimit(int value)
		{
			this._ageLimit = value;
		}
		
		public int getAgeLimit()
		{
			return this._ageLimit;
		}
		
		public void setServerType(int value)
		{
			this._serverType = value;
		}
		
		public int getServerType()
		{
			return this._serverType;
		}
		
		public void setShowingBrackets(boolean value)
		{
			this._isShowingBrackets = value;
		}
		
		public boolean isShowingBrackets()
		{
			return this._isShowingBrackets;
		}
		
		public void setDown()
		{
			this.setAuthed(false);
			this.setPort(0);
			this.setGameServerThread(null);
			this.setStatus(4);
		}
		
		public void addServerAddress(String subnet, String addr) throws UnknownHostException
		{
			this._addrs.add(new GameServerTable.GameServerInfo.GameServerAddress(subnet, addr));
		}
		
		@SuppressWarnings("unlikely-arg-type")
		public String getServerAddress(InetAddress addr)
		{
			for (GameServerTable.GameServerInfo.GameServerAddress a : _addrs)
			{
 
				if (a.equals(addr))
				{
					return a.getServerAddress();
				}
			}
			
			return null;
		}
		
		public String[] getServerAddresses()
		{
			String[] result = new String[this._addrs.size()];
			
			for (int i = 0; i < result.length; i++)
			{
				result[i] = this._addrs.get(i).toString();
			}
			
			return result;
		}
		
		public void clearServerAddresses()
		{
			this._addrs.clear();
		}
		
		private class GameServerAddress extends Subnet
		{
			private final String _serverAddress;
			
			public GameServerAddress(String subnet, String address) throws UnknownHostException
			{
				Objects.requireNonNull(GameServerInfo.this);
				super(subnet);
				this._serverAddress = address;
			}
			
			public String getServerAddress()
			{
				return this._serverAddress;
			}
			
			@Override
			public String toString()
			{
				return this._serverAddress + "/" + super.toString();
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerTable INSTANCE = new GameServerTable();
	}
}
