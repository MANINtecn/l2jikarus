package net.sf.l2jdev.gameserver.data.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class CharInfoTable
{
	private static final Logger LOGGER = Logger.getLogger(CharInfoTable.class.getName());
	private final Map<Integer, String> _names = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _accessLevels = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _levels = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _classes = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _clans = new ConcurrentHashMap<>();
	private final Map<Integer, Map<Integer, String>> _memos = new ConcurrentHashMap<>();
	private final Map<Integer, Calendar> _creationDates = new ConcurrentHashMap<>();
	private final Map<Integer, Long> _lastAccess = new ConcurrentHashMap<>();

	protected CharInfoTable()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT charId, char_name, accesslevel FROM characters");)
		{
			while (rs.next())
			{
				int id = rs.getInt("charId");
				this._names.put(id, rs.getString("char_name"));
				this._accessLevels.put(id, rs.getInt("accesslevel"));
			}
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't retrieve all char id/name/access: " + var12.getMessage(), var12);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._names.size() + " char names.");
	}

	public void addName(Player player)
	{
		if (player != null)
		{
			this.addName(player.getObjectId(), player.getName());
			this._accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
		}
	}

	private void addName(int objectId, String name)
	{
		if (name != null && !name.equals(this._names.get(objectId)))
		{
			this._names.put(objectId, name);
		}
	}

	public void removeName(int objId)
	{
		this._names.remove(objId);
		this._accessLevels.remove(objId);
	}

	public int getIdByName(String name)
	{
		if (name != null && !name.isEmpty())
		{
			for (Entry<Integer, String> entry : this._names.entrySet())
			{
				if (entry.getValue().equalsIgnoreCase(name))
				{
					return entry.getKey();
				}
			}

			int id = -1;
			int accessLevel = 0;

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT charId,accesslevel FROM characters WHERE char_name=?");)
			{
				ps.setString(1, name);

				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						id = rs.getInt("charId");
						accessLevel = rs.getInt("accesslevel");
					}
				}
			}
			catch (SQLException var15)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char name: " + var15.getMessage(), var15);
			}

			if (id > 0)
			{
				this._names.put(id, name);
				this._accessLevels.put(id, accessLevel);
				return id;
			}
			return -1;
		}
		return -1;
	}

	public String getNameById(int id)
	{
		if (id <= 0)
		{
			return null;
		}
		String name = this._names.get(id);
		if (name != null)
		{
			return name;
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT char_name,accesslevel FROM characters WHERE charId=?");)
		{
			ps.setInt(1, id);

			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					name = rset.getString("char_name");
					this._names.put(id, name);
					this._accessLevels.put(id, rset.getInt("accesslevel"));
					return name;
				}
				return null;
			}
		}
		catch (SQLException var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char id: " + var14.getMessage(), var14);
			return null;
		}
	}

	public int getAccessLevelById(int objectId)
	{
		return this.getNameById(objectId) != null ? this._accessLevels.get(objectId) : 0;
	}

	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = false;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as count FROM characters WHERE char_name=?");)
		{
			ps.setString(1, name);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					result = rs.getInt("count") > 0;
				}
			}
		}
		catch (SQLException var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing charname: " + var14.getMessage(), var14);
		}

		return result;
	}

	public int getAccountCharacterCount(String account)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(char_name) as count FROM characters WHERE account_name=?");)
		{
			ps.setString(1, account);

			try (ResultSet rset = ps.executeQuery())
			{
				return rset.next() ? rset.getInt("count") : 0;
			}
		}
		catch (SQLException var13)
		{
			LOGGER.log(Level.WARNING, "Couldn't retrieve account for id: " + var13.getMessage(), var13);
			return 0;
		}
	}

	public void setLevel(int objectId, int level)
	{
		this._levels.put(objectId, level);
	}

	public int getLevelById(int objectId)
	{
		Integer level = this._levels.get(objectId);
		if (level != null)
		{
			return level;
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT level FROM characters WHERE charId = ?");)
		{
			ps.setInt(1, objectId);

			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					int dbLevel = rset.getInt("level");
					this._levels.put(objectId, dbLevel);
					return dbLevel;
				}
				return 0;
			}
		}
		catch (SQLException var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char count: " + var14.getMessage(), var14);
			return 0;
		}
	}

	public void setClassId(int objectId, int classId)
	{
		this._classes.put(objectId, classId);
	}

	public int getClassIdById(int objectId)
	{
		Integer classId = this._classes.get(objectId);
		if (classId != null)
		{
			return classId;
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT classid FROM characters WHERE charId = ?");)
		{
			ps.setInt(1, objectId);

			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					int dbClassId = rset.getInt("classid");
					this._classes.put(objectId, dbClassId);
					return dbClassId;
				}
				return 0;
			}
		}
		catch (SQLException var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't retrieve class for id: " + var14.getMessage(), var14);
			return 0;
		}
	}

	public void setClanId(int objectId, int clanId)
	{
		this._clans.put(objectId, clanId);
	}

	public void removeClanId(int objectId)
	{
		this._clans.remove(objectId);
	}

	public int getClanIdById(int objectId)
	{
		Integer clanId = this._clans.get(objectId);
		if (clanId != null)
		{
			return clanId;
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT clanId FROM characters WHERE charId = ?");)
		{
			ps.setInt(1, objectId);

			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					int dbClanId = rset.getInt("clanId");
					this._clans.put(objectId, dbClanId);
					return dbClanId;
				}
			}
		}
		catch (SQLException var14)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char count: " + var14.getMessage(), var14);
		}

		this._clans.put(objectId, 0);
		return 0;
	}

	public void setFriendMemo(int charId, int friendId, String memo)
	{
		Map<Integer, String> memos = this._memos.get(charId);
		if (memos == null)
		{
			memos = new ConcurrentHashMap<>();
			this._memos.put(charId, memos);
		}

		if (memo == null)
		{
			memos.put(friendId, "");
		}
		else
		{
			String text = memo.toLowerCase();
			if (text.contains("action") && text.contains("bypass"))
			{
				memos.put(friendId, "");
			}
			else
			{
				memos.put(friendId, memo.replaceAll("<.*?>", ""));
			}
		}
	}

	public void removeFriendMemo(int charId, int friendId)
	{
		Map<Integer, String> memos = this._memos.get(charId);
		if (memos != null)
		{
			memos.remove(friendId);
		}
	}

	public String getFriendMemo(int charId, int friendId)
	{
		Map<Integer, String> memos = this._memos.get(charId);
		if (memos == null)
		{
			memos = new ConcurrentHashMap<>();
			this._memos.put(charId, memos);
		}
		else if (memos.containsKey(friendId))
		{
			return memos.get(friendId);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT memo FROM character_friends WHERE charId=? AND friendId=?");)
		{
			statement.setInt(1, charId);
			statement.setInt(2, friendId);

			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					String dbMemo = rset.getString("memo");
					memos.put(friendId, dbMemo == null ? "" : dbMemo);
					return dbMemo;
				}
			}
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Error occurred while retrieving memo: " + var15.getMessage(), var15);
		}

		memos.put(friendId, "");
		return null;
	}

	public Calendar getCharacterCreationDate(int objectId)
	{
		Calendar calendar = this._creationDates.get(objectId);
		if (calendar != null)
		{
			return calendar;
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT createDate FROM characters WHERE charId = ?");)
		{
			ps.setInt(1, objectId);

			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					Date createDate = rset.getDate("createDate");
					Calendar newCalendar = Calendar.getInstance();
					newCalendar.setTime(createDate);
					this._creationDates.put(objectId, newCalendar);
					return newCalendar;
				}
				return null;
			}
		}
		catch (SQLException var15)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not retrieve character creation date: " + var15.getMessage(), var15);
			return null;
		}
	}

	public void setLastAccess(int objectId, long lastAccess)
	{
		this._lastAccess.put(objectId, lastAccess);
	}

	public int getLastAccessDelay(int objectId)
	{
		Long lastAccess = this._lastAccess.get(objectId);
		if (lastAccess != null)
		{
			long currentTime = System.currentTimeMillis();
			long timeDifferenceInMillis = currentTime - lastAccess;
			return (int) (timeDifferenceInMillis / 1000L);
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT lastAccess FROM characters WHERE charId = ?");)
		{
			ps.setInt(1, objectId);

			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					long dbLastAccess = rset.getLong("lastAccess");
					this._lastAccess.put(objectId, dbLastAccess);
					long currentTime = System.currentTimeMillis();
					long timeDifferenceInMillis = currentTime - dbLastAccess;
					return (int) (timeDifferenceInMillis / 1000L);
				}
				return 0;
			}
		}
		catch (SQLException var19)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not retrieve lastAccess timestamp: " + var19.getMessage(), var19);
			return 0;
		}
	}

	public static CharInfoTable getInstance()
	{
		return CharInfoTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CharInfoTable INSTANCE = new CharInfoTable();
	}
}
