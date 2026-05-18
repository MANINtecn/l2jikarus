package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeApplicantInfo;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeRecruitInfo;
import net.sf.l2jdev.gameserver.model.clan.entry.PledgeWaitingInfo;
import net.sf.l2jdev.gameserver.util.MathUtil;

public class ClanEntryManager
{
	protected static final Logger LOGGER = Logger.getLogger(ClanEntryManager.class.getName());
	private static final Map<Integer, PledgeWaitingInfo> _waitingList = new ConcurrentHashMap<>();
	private static final Map<Integer, PledgeRecruitInfo> _clanList = new ConcurrentHashMap<>();
	private static final Map<Integer, Map<Integer, PledgeApplicantInfo>> _applicantList = new ConcurrentHashMap<>();
	private static final Map<Integer, ScheduledFuture<?>> _clanLocked = new ConcurrentHashMap<>();
	private static final Map<Integer, ScheduledFuture<?>> _playerLocked = new ConcurrentHashMap<>();
	public static final String INSERT_APPLICANT = "REPLACE INTO pledge_applicant VALUES (?, ?, ?, ?)";
	public static final String DELETE_APPLICANT = "DELETE FROM pledge_applicant WHERE charId = ? AND clanId = ?";
	public static final String INSERT_WAITING_LIST = "INSERT INTO pledge_waiting_list VALUES (?, ?)";
	public static final String DELETE_WAITING_LIST = "DELETE FROM pledge_waiting_list WHERE char_id = ?";
	public static final String INSERT_CLAN_RECRUIT = "INSERT INTO pledge_recruit VALUES (?, ?, ?, ?, ?, ?)";
	public static final String UPDATE_CLAN_RECRUIT = "UPDATE pledge_recruit SET karma = ?, information = ?, detailed_information = ?, application_type = ?, recruit_type = ? WHERE clan_id = ?";
	public static final String DELETE_CLAN_RECRUIT = "DELETE FROM pledge_recruit WHERE clan_id = ?";
	private static final List<Comparator<PledgeWaitingInfo>> PLAYER_COMPARATOR = Arrays.asList(null, Comparator.comparing(PledgeWaitingInfo::getPlayerName), Comparator.comparingInt(PledgeWaitingInfo::getKarma), Comparator.comparingInt(PledgeWaitingInfo::getPlayerLvl), Comparator.comparingInt(PledgeWaitingInfo::getPlayerClassId));
	private static final List<Comparator<PledgeRecruitInfo>> CLAN_COMPARATOR = Arrays.asList(null, Comparator.comparing(PledgeRecruitInfo::getClanName), Comparator.comparing(PledgeRecruitInfo::getClanLeaderName), Comparator.comparingInt(PledgeRecruitInfo::getClanLevel), Comparator.comparingInt(PledgeRecruitInfo::getKarma));
	private static final long LOCK_TIME = TimeUnit.MINUTES.toMillis(5L);

	protected ClanEntryManager()
	{
		this.load();
	}

	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM pledge_recruit");)
		{
			while (rs.next())
			{
				int clanId = rs.getInt("clan_id");
				_clanList.put(clanId, new PledgeRecruitInfo(clanId, rs.getInt("karma"), rs.getString("information"), rs.getString("detailed_information"), rs.getInt("application_type"), rs.getInt("recruit_type")));
				if (ClanTable.getInstance().getClan(clanId) == null)
				{
					this.removeFromClanList(clanId);
				}
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _clanList.size() + " clan entries.");
		}
		catch (Exception var26)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to load: ", var26);
		}

		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT a.char_id, a.karma, b.base_class, b.level, b.char_name FROM pledge_waiting_list as a LEFT JOIN characters as b ON a.char_id = b.charId");)
		{
			while (rs.next())
			{
				_waitingList.put(rs.getInt("char_id"), new PledgeWaitingInfo(rs.getInt("char_id"), rs.getInt("level"), rs.getInt("karma"), rs.getInt("base_class"), rs.getString("char_name")));
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _waitingList.size() + " players in waiting list.");
		}
		catch (Exception var22)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to load: ", var22);
		}

		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT a.charId, a.clanId, a.karma, a.message, b.base_class, b.level, b.char_name FROM pledge_applicant as a LEFT JOIN characters as b ON a.charId = b.charId");)
		{
			while (rs.next())
			{
				_applicantList.computeIfAbsent(rs.getInt("clanId"), _ -> new ConcurrentHashMap<>()).put(rs.getInt("charId"), new PledgeApplicantInfo(rs.getInt("charId"), rs.getString("char_name"), rs.getInt("level"), rs.getInt("karma"), rs.getInt("clanId"), rs.getString("message")));
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _applicantList.size() + " player applications.");
		}
		catch (Exception var18)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to load: ", var18);
		}
	}

	public Map<Integer, PledgeWaitingInfo> getWaitingList()
	{
		return _waitingList;
	}

	public Map<Integer, PledgeRecruitInfo> getClanList()
	{
		return _clanList;
	}

	public Map<Integer, Map<Integer, PledgeApplicantInfo>> getApplicantList()
	{
		return _applicantList;
	}

	public Map<Integer, PledgeApplicantInfo> getApplicantListForClan(int clanId)
	{
		return _applicantList.getOrDefault(clanId, Collections.emptyMap());
	}

	public PledgeApplicantInfo getPlayerApplication(int clanId, int playerId)
	{
		return _applicantList.getOrDefault(clanId, Collections.emptyMap()).get(playerId);
	}

	public boolean removePlayerApplication(int clanId, int playerId)
	{
		Map<Integer, PledgeApplicantInfo> clanApplicantList = _applicantList.get(clanId);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM pledge_applicant WHERE charId = ? AND clanId = ?");)
		{
			statement.setInt(1, playerId);
			statement.setInt(2, clanId);
			statement.executeUpdate();
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, var12.getMessage(), var12);
		}

		return clanApplicantList != null && clanApplicantList.remove(playerId) != null;
	}

	public boolean addPlayerApplicationToClan(int clanId, PledgeApplicantInfo info)
	{
		if (!_playerLocked.containsKey(info.getPlayerId()))
		{
			_applicantList.computeIfAbsent(clanId, _ -> new ConcurrentHashMap<>()).put(info.getPlayerId(), info);

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO pledge_applicant VALUES (?, ?, ?, ?)");)
			{
				statement.setInt(1, info.getPlayerId());
				statement.setInt(2, info.getRequestClanId());
				statement.setInt(3, info.getKarma());
				statement.setString(4, info.getMessage());
				statement.executeUpdate();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, var11.getMessage(), var11);
			}

			return true;
		}
		return false;
	}

	public Integer getClanIdForPlayerApplication(int playerId)
	{
		for (Entry<Integer, Map<Integer, PledgeApplicantInfo>> entry : _applicantList.entrySet())
		{
			if (entry.getValue().containsKey(playerId))
			{
				return entry.getKey();
			}
		}

		return 0;
	}

	public boolean addToWaitingList(int playerId, PledgeWaitingInfo info)
	{
		if (!_playerLocked.containsKey(playerId))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO pledge_waiting_list VALUES (?, ?)");)
			{
				statement.setInt(1, info.getPlayerId());
				statement.setInt(2, info.getKarma());
				statement.executeUpdate();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, var11.getMessage(), var11);
			}

			_waitingList.put(playerId, info);
			return true;
		}
		return false;
	}

	public boolean removeFromWaitingList(int playerId)
	{
		if (_waitingList.containsKey(playerId))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM pledge_waiting_list WHERE char_id = ?");)
			{
				statement.setInt(1, playerId);
				statement.executeUpdate();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.WARNING, var10.getMessage(), var10);
			}

			_waitingList.remove(playerId);
			lockPlayer(playerId);
			return true;
		}
		return false;
	}

	public boolean addToClanList(int clanId, PledgeRecruitInfo info)
	{
		if (!_clanList.containsKey(clanId) && !_clanLocked.containsKey(clanId))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO pledge_recruit VALUES (?, ?, ?, ?, ?, ?)");)
			{
				statement.setInt(1, info.getClanId());
				statement.setInt(2, info.getKarma());
				statement.setString(3, info.getInformation());
				statement.setString(4, info.getDetailedInformation());
				statement.setInt(5, info.getApplicationType());
				statement.setInt(6, info.getRecruitType());
				statement.executeUpdate();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, var11.getMessage(), var11);
			}

			_clanList.put(clanId, info);
			return true;
		}
		return false;
	}

	public boolean updateClanList(int clanId, PledgeRecruitInfo info)
	{
		if (_clanList.containsKey(clanId) && !_clanLocked.containsKey(clanId))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE pledge_recruit SET karma = ?, information = ?, detailed_information = ?, application_type = ?, recruit_type = ? WHERE clan_id = ?");)
			{
				statement.setInt(1, info.getKarma());
				statement.setString(2, info.getInformation());
				statement.setString(3, info.getDetailedInformation());
				statement.setInt(4, info.getApplicationType());
				statement.setInt(5, info.getRecruitType());
				statement.setInt(6, info.getClanId());
				statement.executeUpdate();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, var11.getMessage(), var11);
			}

			return _clanList.replace(clanId, info) != null;
		}
		return false;
	}

	public boolean removeFromClanList(int clanId)
	{
		if (_clanList.containsKey(clanId))
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM pledge_recruit WHERE clan_id = ?");)
			{
				statement.setInt(1, clanId);
				statement.executeUpdate();
			}
			catch (Exception var10)
			{
				LOGGER.log(Level.WARNING, var10.getMessage(), var10);
			}

			_clanList.remove(clanId);
			lockClan(clanId);
			return true;
		}
		return false;
	}

	public List<PledgeWaitingInfo> getSortedWaitingList(int levelMin, int levelMax, int role, int sortByValue, boolean descending)
	{
		int sortBy = MathUtil.clamp(sortByValue, 1, PLAYER_COMPARATOR.size() - 1);
		List<PledgeWaitingInfo> result = new ArrayList<>();

		for (PledgeWaitingInfo p : _waitingList.values())
		{
			if (p.getPlayerLvl() >= levelMin && p.getPlayerLvl() <= levelMax)
			{
				result.add(p);
			}
		}

		result.sort(descending ? PLAYER_COMPARATOR.get(sortBy).reversed() : PLAYER_COMPARATOR.get(sortBy));
		return result;
	}

	public List<PledgeWaitingInfo> queryWaitingListByName(String name)
	{
		List<PledgeWaitingInfo> result = new ArrayList<>();

		for (PledgeWaitingInfo p : _waitingList.values())
		{
			if (p.getPlayerName().toLowerCase().contains(name))
			{
				result.add(p);
			}
		}

		return result;
	}

	public List<PledgeRecruitInfo> getSortedClanListByName(String query, int type)
	{
		List<PledgeRecruitInfo> result = new ArrayList<>();
		if (type == 1)
		{
			for (PledgeRecruitInfo p : _clanList.values())
			{
				if (p.getClanName().toLowerCase().contains(query))
				{
					result.add(p);
				}
			}
		}
		else
		{
			for (PledgeRecruitInfo px : _clanList.values())
			{
				if (px.getClanLeaderName().toLowerCase().contains(query))
				{
					result.add(px);
				}
			}
		}

		return result;
	}

	public PledgeRecruitInfo getClanById(int clanId)
	{
		return _clanList.get(clanId);
	}

	public boolean isClanRegistred(int clanId)
	{
		return _clanList.get(clanId) != null;
	}

	public boolean isPlayerRegistred(int playerId)
	{
		return _waitingList.get(playerId) != null;
	}

	public List<PledgeRecruitInfo> getUnSortedClanList()
	{
		return new ArrayList<>(_clanList.values());
	}

	public List<PledgeRecruitInfo> getSortedClanList(int clanLevel, int karma, int sortByValue, boolean descending)
	{
		int sortBy = MathUtil.clamp(sortByValue, 1, CLAN_COMPARATOR.size() - 1);
		List<PledgeRecruitInfo> sortedList = new ArrayList<>(_clanList.values());

		for (int i = 0; i < sortedList.size(); i++)
		{
			PledgeRecruitInfo currentInfo = sortedList.get(i);
			if (clanLevel < 0 && karma >= 0 && karma != currentInfo.getKarma() || clanLevel >= 0 && karma < 0 && clanLevel != (currentInfo.getClan() != null ? currentInfo.getClanLevel() : 0) || clanLevel >= 0 && karma >= 0 && (clanLevel != (currentInfo.getClan() != null ? currentInfo.getClanLevel() : 0) || karma != currentInfo.getKarma()))
			{
				sortedList.remove(i--);
			}
		}

		Collections.sort(sortedList, descending ? CLAN_COMPARATOR.get(sortBy).reversed() : CLAN_COMPARATOR.get(sortBy));
		return sortedList;
	}

	public long getPlayerLockTime(int playerId)
	{
		return _playerLocked.get(playerId) == null ? 0L : _playerLocked.get(playerId).getDelay(TimeUnit.MINUTES);
	}

	public long getClanLockTime(int playerId)
	{
		return _clanLocked.get(playerId) == null ? 0L : _clanLocked.get(playerId).getDelay(TimeUnit.MINUTES);
	}

	private static void lockPlayer(int playerId)
	{
		_playerLocked.put(playerId, ThreadPool.schedule(() -> _playerLocked.remove(playerId), LOCK_TIME));
	}

	private static void lockClan(int clanId)
	{
		_clanLocked.put(clanId, ThreadPool.schedule(() -> _clanLocked.remove(clanId), LOCK_TIME));
	}

	public static ClanEntryManager getInstance()
	{
		return ClanEntryManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ClanEntryManager INSTANCE = new ClanEntryManager();
	}
}
