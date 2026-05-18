package net.sf.l2jdev.gameserver.model.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadInfo;
import net.sf.l2jdev.gameserver.util.Broadcast;
import net.sf.l2jdev.gameserver.util.MathUtil;

public class Olympiad extends ListenersContainer
{
	protected static final Logger LOGGER = Logger.getLogger(Olympiad.class.getName());
	protected static final Logger LOGGER_OLYMPIAD = Logger.getLogger("olympiad");
	private static final Map<Integer, StatSet> NOBLES = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> NOBLES_RANK = new HashMap<>();
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	public static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle, period, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0";
	public static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle, period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?";
	public static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn, olympiad_nobles.competitions_done_week FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId";
	public static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`charId`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`, `competitions_done_week`) VALUES (?,?,?,?,?,?,?,?)";
	public static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ?, competitions_done_week = ? WHERE charId = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.charId, characters.char_name FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id in (?, ?) AND olympiad_nobles.competitions_done >= " + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
	private static final String OLYMPIAD_GET_LEGEND = "SELECT olympiad_nobles.charId FROM olympiad_nobles WHERE olympiad_nobles.competitions_done >=" + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " ORDER BY olympiad_nobles.olympiad_points DESC LIMIT 1";
	private static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT charId from olympiad_nobles_eom WHERE competitions_done >= " + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.charId = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= " + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT = "SELECT characters.char_name from olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.charId = olympiad_nobles_eom.charId AND (olympiad_nobles_eom.class_id = ? OR olympiad_nobles_eom.class_id = 133) AND olympiad_nobles_eom.competitions_done >= " + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND = "SELECT characters.char_name from olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND (olympiad_nobles.class_id = ? OR olympiad_nobles.class_id = 133) AND olympiad_nobles.competitions_done >= " + OlympiadConfig.OLYMPIAD_MIN_MATCHES + " ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
	public static final String REMOVE_UNCLAIMED_POINTS = "DELETE FROM character_variables WHERE charId=? AND var=?";
	public static final String INSERT_UNCLAIMED_POINTS = "INSERT INTO character_variables (charId, var, val) VALUES (?, ?, ?)";
	public static final String UNCLAIMED_OLYMPIAD_POINTS_VAR = "UNCLAIMED_OLYMPIAD_POINTS";
	public static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	public static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	public static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT charId, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles";
	private static final Set<Integer> HERO_IDS = CategoryData.getInstance().getCategoryByType(CategoryType.FOURTH_CLASS_GROUP);
	private static final int COMP_START = OlympiadConfig.OLYMPIAD_START_TIME;
	private static final int COMP_MIN = OlympiadConfig.OLYMPIAD_MIN;
	private static final long COMP_PERIOD = OlympiadConfig.OLYMPIAD_CPERIOD;
	protected static final long WEEKLY_PERIOD = OlympiadConfig.OLYMPIAD_WPERIOD;
	protected static final long VALIDATION_PERIOD = OlympiadConfig.OLYMPIAD_VPERIOD;
	public static final int DEFAULT_POINTS = OlympiadConfig.OLYMPIAD_START_POINTS;
	protected static final int WEEKLY_POINTS = OlympiadConfig.OLYMPIAD_WEEKLY_POINTS;
	public static final String CHAR_ID = "charId";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	public static final String COMP_DONE_WEEK = "competitions_done_week";
	protected long _olympiadEnd;
	protected long _validationEnd;
	protected int _period;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	private long _compEnd;
	private Calendar _compStart;
	protected static boolean _inCompPeriod;
	protected static boolean _compStarted = false;
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValdationTask;
	protected ScheduledFuture<?> _gameManager = null;
	protected ScheduledFuture<?> _gameAnnouncer = null;

	protected Olympiad()
	{
		if (OlympiadConfig.OLYMPIAD_ENABLED)
		{
			this.load();
			AntiFeedManager.getInstance().registerEvent(1);
			if (this._period == 0)
			{
				this.init();
			}
		}
		else
		{
			LOGGER.log(Level.INFO, "Disabled.");
		}
	}

	private void load()
	{
		NOBLES.clear();
		boolean loaded = false;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT current_cycle, period, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0"); ResultSet rset = statement.executeQuery();)
		{
			while (rset.next())
			{
				this._currentCycle = rset.getInt("current_cycle");
				this._period = rset.getInt("period");
				this._olympiadEnd = rset.getLong("olympiad_end");
				this._validationEnd = rset.getLong("validation_end");
				this._nextWeeklyChange = rset.getLong("next_weekly_change");
				loaded = true;
			}
		}
		catch (Exception var33)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error loading olympiad data from database: ", var33);
		}

		if (!loaded)
		{
			this._currentCycle = 1;
			this._period = 0;
			this._olympiadEnd = 0L;
			this._validationEnd = 0L;
			this._nextWeeklyChange = 0L;
		}

		long currentTime = System.currentTimeMillis();
		switch (this._period)
		{
			case 0:
				if (this._olympiadEnd != 0L && this._olympiadEnd >= currentTime)
				{
					this.scheduleWeeklyChange();
				}
				else
				{
					this.setNewOlympiadEnd();
				}
				break;
			case 1:
				if (this._validationEnd > currentTime)
				{
					this.loadNoblesRank();
					this._scheduledValdationTask = ThreadPool.schedule(new Olympiad.ValidationEndTask(), this.getMillisToValidationEnd());
				}
				else
				{
					this._currentCycle++;
					this._period = 0;
					this.deleteNobles();
					this.setNewOlympiadEnd();
				}
				break;
			default:
				LOGGER.warning("Olympiad System: Omg something went wrong in loading!! Period = " + this._period);
				return;
		}

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT olympiad_nobles.charId, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn, olympiad_nobles.competitions_done_week FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId");
			ResultSet rset = statement.executeQuery();)
		{
			while (rset.next())
			{
				StatSet statData = new StatSet();
				statData.set("class_id", rset.getInt("class_id"));
				statData.set("char_name", rset.getString("char_name"));
				int compDone = rset.getInt("competitions_done");
				statData.set("olympiad_points", MathUtil.clamp(rset.getInt("olympiad_points"), 0, OlympiadConfig.OLYMPIAD_MAX_POINTS * compDone + OlympiadConfig.OLYMPIAD_WEEKLY_POINTS * 4));
				statData.set("competitions_done", compDone);
				statData.set("competitions_won", rset.getInt("competitions_won"));
				statData.set("competitions_lost", rset.getInt("competitions_lost"));
				statData.set("competitions_drawn", rset.getInt("competitions_drawn"));
				statData.set("competitions_done_week", rset.getInt("competitions_done_week"));
				statData.set("to_save", false);
				addNobleStats(rset.getInt("charId"), statData);
			}
		}
		catch (Exception var29)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error loading noblesse data from database: ", var29);
		}

		LOGGER.info("Olympiad System: Loading....");
		if (this._period == 0)
		{
			LOGGER.info("Olympiad System: Currently in Olympiad Period");
		}
		else
		{
			LOGGER.info("Olympiad System: Currently in Validation Period");
		}

		long milliToEnd;
		if (this._period == 0)
		{
			milliToEnd = this.getMillisToOlympiadEnd();
		}
		else
		{
			milliToEnd = this.getMillisToValidationEnd();
		}

		double numSecs = milliToEnd / 1000L % 60L;
		double countDown = (milliToEnd / 1000.0 - numSecs) / 60.0;
		int numMins = (int) Math.floor(countDown % 60.0);
		countDown = (countDown - numMins) / 60.0;
		int numHours = (int) Math.floor(countDown % 24.0);
		int numDays = (int) Math.floor((countDown - numHours) / 24.0);
		LOGGER.info("Olympiad System: " + numDays + " days, " + numHours + " hours and " + numMins + " mins until period ends.");
		if (this._period == 0)
		{
			milliToEnd = this.getMillisToWeekChange();
			double numSecs2 = milliToEnd / 1000L % 60L;
			double countDown2 = (milliToEnd / 1000.0 - numSecs2) / 60.0;
			int numMins2 = (int) Math.floor(countDown % 60.0);
			countDown2 = (countDown2 - numMins) / 60.0;
			int numHours2 = (int) Math.floor(countDown2 % 24.0);
			int numDays2 = (int) Math.floor((countDown2 - numHours) / 24.0);
			LOGGER.info("Olympiad System: Next weekly change is in " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
		}

		LOGGER.info("Olympiad System: Loaded " + NOBLES.size() + " Nobles");
	}

	public int getOlympiadRank(Player player)
	{
		return NOBLES_RANK.getOrDefault(player.getObjectId(), 0);
	}

	public void loadNoblesRank()
	{
		NOBLES_RANK.clear();
		Map<Integer, Integer> tmpPlace = new HashMap<>();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS); ResultSet rset = statement.executeQuery();)
		{
			int place = 1;

			while (rset.next())
			{
				tmpPlace.put(rset.getInt("charId"), place++);
			}
		}
		catch (Exception var30)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error loading noblesse data from database for Ranking: ", var30);
		}

		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.1);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.5);
		if (rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}

		for (Entry<Integer, Integer> chr : tmpPlace.entrySet())
		{
			if (chr.getValue() <= rank1)
			{
				NOBLES_RANK.put(chr.getKey(), 1);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank2)
			{
				NOBLES_RANK.put(chr.getKey(), 2);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank3)
			{
				NOBLES_RANK.put(chr.getKey(), 3);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank4)
			{
				NOBLES_RANK.put(chr.getKey(), 4);
			}
			else
			{
				NOBLES_RANK.put(chr.getKey(), 5);
			}
		}

		for (int noblesId : NOBLES.keySet())
		{
			int points = this.getOlympiadTradePoint(noblesId);
			if (points > 0)
			{
				Player player = World.getInstance().getPlayer(noblesId);
				if (player != null)
				{
					player.getVariables().set("UNCLAIMED_OLYMPIAD_POINTS", points);
				}
				else
				{
					try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE charId=? AND var=?");)
					{
						statement.setInt(1, noblesId);
						statement.setString(2, "UNCLAIMED_OLYMPIAD_POINTS");
						statement.execute();
					}
					catch (SQLException var26)
					{
						LOGGER.warning("Olympiad System: Couldn't remove unclaimed olympiad points from DB!");
					}

					try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_variables (charId, var, val) VALUES (?, ?, ?)");)
					{
						statement.setInt(1, noblesId);
						statement.setString(2, "UNCLAIMED_OLYMPIAD_POINTS");
						statement.setString(3, String.valueOf(points));
						statement.execute();
					}
					catch (SQLException var23)
					{
						LOGGER.warning("Olympiad System: Couldn't store unclaimed olympiad points to DB!");
					}
				}
			}
		}
	}

	protected void init()
	{
		if (this._period != 1)
		{
			this._compStart = Calendar.getInstance();
			int currentDay = this._compStart.get(7);
			boolean dayFound = false;
			int dayCounter = 0;

			for (int i = currentDay; i < 8; i++)
			{
				if (OlympiadConfig.OLYMPIAD_COMPETITION_DAYS.contains(i))
				{
					dayFound = true;
					break;
				}

				dayCounter++;
			}

			if (!dayFound)
			{
				for (int i = 1; i < 8 && !OlympiadConfig.OLYMPIAD_COMPETITION_DAYS.contains(i); i++)
				{
					dayCounter++;
				}
			}

			if (dayCounter > 0)
			{
				this._compStart.add(5, dayCounter);
			}

			this._compStart.set(11, COMP_START);
			this._compStart.set(12, COMP_MIN);
			this._compEnd = this._compStart.getTimeInMillis() + COMP_PERIOD;
			if (this._scheduledOlympiadEnd != null)
			{
				this._scheduledOlympiadEnd.cancel(true);
			}

			this._scheduledOlympiadEnd = ThreadPool.schedule(new Olympiad.OlympiadEndTask(), this.getMillisToOlympiadEnd());
			this.updateCompStatus();
		}
	}

	public int getRemainingTime()
	{
		return (int) ((this._compEnd - Calendar.getInstance().getTimeInMillis()) / 1000L);
	}

	protected static int getNobleCount()
	{
		return NOBLES.size();
	}

	public static StatSet getNobleStats(int playerId)
	{
		return NOBLES.get(playerId);
	}

	public static void removeNobleStats(int playerId)
	{
		NOBLES.remove(playerId);
		NOBLES_RANK.remove(playerId);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps1 = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?"); PreparedStatement ps2 = con.prepareStatement("DELETE FROM olympiad_nobles_eom WHERE charId=?");)
		{
			ps1.setInt(1, playerId);
			ps2.setInt(1, playerId);
			ps1.execute();
			ps2.execute();
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error removing noblesse data from database: ", var12);
		}
	}

	private void updateCompStatus()
	{
		long milliToStart = this.getMillisToCompBegin();
		double numSecs = milliToStart / 1000L % 60L;
		double countDown = (milliToStart / 1000.0 - numSecs) / 60.0;
		int numMins = (int) Math.floor(countDown % 60.0);
		countDown = (countDown - numMins) / 60.0;
		int numHours = (int) Math.floor(countDown % 24.0);
		int numDays = (int) Math.floor((countDown - numHours) / 24.0);
		LOGGER.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		LOGGER.info("Olympiad System: Event starts/started: " + this._compStart.getTime());
		this._scheduledCompStart = ThreadPool.schedule(() -> {
			if (!this.isOlympiadEnd() && OlympiadConfig.OLYMPIAD_ENABLED)
			{
				_inCompPeriod = true;

				for (Player player : World.getInstance().getPlayers())
				{
					player.sendPacket(new ExOlympiadInfo(1, this.getRemainingTime()));
				}

				Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.THE_OLYMPIAD_HAS_BEGUN));
				LOGGER.info("Olympiad System: Olympiad Games have started.");
				LOGGER_OLYMPIAD.info("Result,Player1,Player2,Player1 HP,Player2 HP,Player1 Damage,Player2 Damage,Points,Classed");
				this._gameManager = ThreadPool.scheduleAtFixedRate(OlympiadGameManager.getInstance(), 30000L, 30000L);
				if (OlympiadConfig.OLYMPIAD_ANNOUNCE_GAMES)
				{
					this._gameAnnouncer = ThreadPool.scheduleAtFixedRate(new OlympiadAnnouncer(), 30000L, 500L);
				}

				long regEnd = this.getMillisToCompEnd() - 600000L;
				if (regEnd > 0L)
				{
					ThreadPool.schedule(() -> Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.THE_OLYMPIAD_REGISTRATION_PERIOD_HAS_ENDED)), regEnd);
				}

				this._scheduledCompEnd = ThreadPool.schedule(() -> {
					if (!this.isOlympiadEnd())
					{
						_inCompPeriod = false;

						for (Player playerx : World.getInstance().getPlayers())
						{
							playerx.sendPacket(new ExOlympiadInfo(0, this.getRemainingTime()));
						}

						Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.THE_OLYMPIAD_IS_OVER));
						LOGGER.info("Olympiad System: Olympiad games have ended.");

						while (OlympiadGameManager.getInstance().isBattleStarted())
						{
							try
							{
								Thread.sleep(60000L);
							}
							catch (Exception var3x)
							{
							}
						}

						if (this._gameManager != null)
						{
							this._gameManager.cancel(false);
							this._gameManager = null;
						}

						if (this._gameAnnouncer != null)
						{
							this._gameAnnouncer.cancel(false);
							this._gameAnnouncer = null;
						}

						this.saveOlympiadStatus();
						this.init();
					}
				}, this.getMillisToCompEnd());
			}
		}, this.getMillisToCompBegin());
	}

	public long getMillisToOlympiadEnd()
	{
		return this._olympiadEnd - System.currentTimeMillis();
	}

	public void manualSelectHeroes()
	{
		if (this._scheduledOlympiadEnd != null)
		{
			this._scheduledOlympiadEnd.cancel(true);
		}

		this._scheduledOlympiadEnd = ThreadPool.schedule(new Olympiad.OlympiadEndTask(), 0L);
	}

	protected long getMillisToValidationEnd()
	{
		long currentTime = System.currentTimeMillis();
		return this._validationEnd > currentTime ? this._validationEnd - currentTime : 10L;
	}

	public boolean isOlympiadEnd()
	{
		return this._period != 0;
	}

	protected void setNewOlympiadEnd()
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.ROUND_S1_OF_THE_OLYMPIAD_GAMES_HAS_STARTED);
		sm.addInt(this._currentCycle);
		Broadcast.toAllOnlinePlayers(sm);
		Calendar currentTime = Calendar.getInstance();
		currentTime.set(9, 0);
		currentTime.set(11, 12);
		currentTime.set(12, 0);
		currentTime.set(13, 0);
		Calendar nextChange = Calendar.getInstance();
		String var4 = OlympiadConfig.OLYMPIAD_PERIOD;
		switch (var4)
		{
			case "DAY":
				currentTime.add(5, OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER);
				currentTime.add(5, -1);
				if (OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER >= 14)
				{
					this._nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				}
				else if (OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER >= 7)
				{
					this._nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD / 2L;
				}
				else
				{
					LOGGER.warning("Invalid config value for OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER, must be >= 7");
				}
				break;
			case "WEEK":
				currentTime.add(4, OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER);
				currentTime.add(5, -1);
				if (OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER > 1)
				{
					this._nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				}
				else
				{
					this._nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD / 2L;
				}
				break;
			case "MONTH":
				currentTime.add(2, OlympiadConfig.OLYMPIAD_PERIOD_MULTIPLIER);
				currentTime.add(5, -1);
				this._nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}

		this._olympiadEnd = currentTime.getTimeInMillis();
		this.scheduleWeeklyChange();
	}

	public boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private long getMillisToCompBegin()
	{
		long currentTime = System.currentTimeMillis();
		if (this._compStart.getTimeInMillis() < currentTime && this._compEnd > currentTime)
		{
			return 10L;
		}
		return this._compStart.getTimeInMillis() > currentTime ? this._compStart.getTimeInMillis() - currentTime : this.setNewCompBegin();
	}

	private long setNewCompBegin()
	{
		this._compStart = Calendar.getInstance();
		int currentDay = this._compStart.get(7);
		this._compStart.set(11, COMP_START);
		this._compStart.set(12, COMP_MIN);
		if (currentDay == this._compStart.get(7))
		{
			if (currentDay == 7)
			{
				currentDay = 1;
			}
			else
			{
				currentDay++;
			}
		}

		boolean dayFound = false;
		int dayCounter = 0;

		for (int i = currentDay; i < 8; i++)
		{
			if (OlympiadConfig.OLYMPIAD_COMPETITION_DAYS.contains(i))
			{
				dayFound = true;
				break;
			}

			dayCounter++;
		}

		if (!dayFound)
		{
			for (int i = 1; i < 8 && !OlympiadConfig.OLYMPIAD_COMPETITION_DAYS.contains(i); i++)
			{
				dayCounter++;
			}
		}

		if (dayCounter > 0)
		{
			this._compStart.add(5, dayCounter);
		}

		this._compStart.add(11, 24);
		this._compEnd = this._compStart.getTimeInMillis() + COMP_PERIOD;
		LOGGER.info("Olympiad System: New Schedule @ " + this._compStart.getTime());
		return this._compStart.getTimeInMillis() - System.currentTimeMillis();
	}

	public long getMillisToCompEnd()
	{
		return this._compEnd - System.currentTimeMillis();
	}

	public long getMillisToWeekChange()
	{
		long currentTime = System.currentTimeMillis();
		return this._nextWeeklyChange > currentTime ? this._nextWeeklyChange - currentTime : 10L;
	}

	private void scheduleWeeklyChange()
	{
		this._scheduledWeeklyTask = ThreadPool.scheduleAtFixedRate(() -> {
			this.addWeeklyPoints();
			LOGGER.info("Olympiad System: Added weekly points to nobles");
			this.resetWeeklyMatches();
			LOGGER.info("Olympiad System: Reset weekly matches to nobles");
			this._nextWeeklyChange = System.currentTimeMillis() + WEEKLY_PERIOD;
		}, this.getMillisToWeekChange(), WEEKLY_PERIOD);
	}

	protected void addWeeklyPoints()
	{
		if (this._period != 1)
		{
			for (StatSet nobleInfo : NOBLES.values())
			{
				nobleInfo.set("olympiad_points", MathUtil.clamp(nobleInfo.getInt("olympiad_points", 0) + WEEKLY_POINTS, 0, nobleInfo.getInt("competitions_done", 0) * OlympiadConfig.OLYMPIAD_MAX_POINTS + OlympiadConfig.OLYMPIAD_WEEKLY_POINTS * 4));
			}
		}
	}

	protected void resetWeeklyMatches()
	{
		if (this._period != 1)
		{
			for (StatSet nobleInfo : NOBLES.values())
			{
				nobleInfo.set("competitions_done_week", 0);
			}
		}
	}

	public int getCurrentCycle()
	{
		return this._currentCycle;
	}

	public int getPeriod()
	{
		return this._period;
	}

	public boolean playerInStadia(Player player)
	{
		return ZoneManager.getInstance().getOlympiadStadium(player) != null;
	}

	protected synchronized void saveNobleData()
	{
		if (!NOBLES.isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				for (Entry<Integer, StatSet> entry : new ArrayList<>(NOBLES.entrySet()))
				{
					StatSet nobleInfo = entry.getValue();
					if (nobleInfo != null)
					{
						int charId = entry.getKey();
						int classId = nobleInfo.getInt("class_id");
						int points = nobleInfo.getInt("olympiad_points");
						int compDone = nobleInfo.getInt("competitions_done");
						int compWon = nobleInfo.getInt("competitions_won");
						int compLost = nobleInfo.getInt("competitions_lost");
						int compDrawn = nobleInfo.getInt("competitions_drawn");
						int compDoneWeek = nobleInfo.getInt("competitions_done_week");
						boolean toSave = nobleInfo.getBoolean("to_save");

						try (
							PreparedStatement statement = con.prepareStatement(toSave ? "INSERT INTO olympiad_nobles (`charId`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`, `competitions_done_week`) VALUES (?,?,?,?,?,?,?,?)" : "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ?, competitions_done_week = ? WHERE charId = ?"))
						{
							if (toSave)
							{
								statement.setInt(1, charId);
								statement.setInt(2, classId);
								statement.setInt(3, points);
								statement.setInt(4, compDone);
								statement.setInt(5, compWon);
								statement.setInt(6, compLost);
								statement.setInt(7, compDrawn);
								statement.setInt(8, compDoneWeek);
								nobleInfo.set("to_save", false);
							}
							else
							{
								statement.setInt(1, points);
								statement.setInt(2, compDone);
								statement.setInt(3, compWon);
								statement.setInt(4, compLost);
								statement.setInt(5, compDrawn);
								statement.setInt(6, compDoneWeek);
								statement.setInt(7, charId);
							}

							statement.execute();
							statement.close();
						}
						catch (SQLException var21)
						{
							LOGGER.log(Level.SEVERE, "Olympiad System: Failed to save noble data for charId " + charId, var21);
						}
					}
				}
			}
			catch (SQLException var23)
			{
				LOGGER.log(Level.SEVERE, "Olympiad System: Failed to save noblesse data to database: ", var23);
			}
		}
	}

	public void saveOlympiadStatus()
	{
		this.saveNobleData();

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO olympiad_data (id, current_cycle, period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?");)
		{
			statement.setInt(1, this._currentCycle);
			statement.setInt(2, this._period);
			statement.setLong(3, this._olympiadEnd);
			statement.setLong(4, this._validationEnd);
			statement.setLong(5, this._nextWeeklyChange);
			statement.setInt(6, this._currentCycle);
			statement.setInt(7, this._period);
			statement.setLong(8, this._olympiadEnd);
			statement.setLong(9, this._validationEnd);
			statement.setLong(10, this._nextWeeklyChange);
			statement.execute();
		}
		catch (SQLException var9)
		{
			LOGGER.log(Level.SEVERE, "Olympiad System: Failed to save olympiad data to database: ", var9);
		}
	}

	protected void updateMonthlyData()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("TRUNCATE olympiad_nobles_eom");
			PreparedStatement ps2 = con.prepareStatement("INSERT INTO olympiad_nobles_eom SELECT charId, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles");)
		{
			ps1.execute();
			ps2.execute();
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.SEVERE, "Olympiad System: Failed to update monthly noblese data: ", var12);
		}
	}

	protected List<StatSet> sortHerosToBe()
	{
		if (this._period != 1)
		{
			return Collections.emptyList();
		}
		LOGGER_OLYMPIAD.info("Noble,charid,classid,compDone,points");

		for (Entry<Integer, StatSet> entry : NOBLES.entrySet())
		{
			StatSet nobleInfo = entry.getValue();
			if (nobleInfo != null)
			{
				int charId = entry.getKey();
				int classId = nobleInfo.getInt("class_id");
				String charName = nobleInfo.getString("char_name");
				int points = nobleInfo.getInt("olympiad_points");
				int compDone = nobleInfo.getInt("competitions_done");
				LOGGER_OLYMPIAD.info(charName + "," + charId + "," + classId + "," + compDone + "," + points);
			}
		}

		List<StatSet> heroesToBe = new LinkedList<>();
		int legendId = 0;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_LEGEND); ResultSet rset = statement.executeQuery();)
		{
			if (rset.next())
			{
				legendId = rset.getInt("charId");
			}
		}
		catch (SQLException var26)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Could not load legend from DB", var26);
		}

		try (Connection conx = DatabaseFactory.getConnection(); PreparedStatement statementx = conx.prepareStatement(OLYMPIAD_GET_HEROS);)
		{
			for (int element : HERO_IDS)
			{
				PlayerClass parent = ClassListData.getInstance().getClass(element).getParentClass();
				statementx.setInt(1, element);
				statementx.setInt(2, parent.getId());

				try (ResultSet rsetx = statementx.executeQuery())
				{
					if (rsetx.next())
					{
						StatSet hero = new StatSet();
						int charId = rsetx.getInt("charId");
						hero.set("class_id", element);
						hero.set("charId", charId);
						hero.set("char_name", rsetx.getString("char_name"));
						hero.set("LEGEND", charId == legendId ? 1 : 0);
						LOGGER_OLYMPIAD.info("Hero " + hero.getString("char_name") + "," + charId + "," + hero.getInt("class_id"));
						heroesToBe.add(hero);
					}
				}
			}
		}
		catch (SQLException var22)
		{
			LOGGER.warning("Olympiad System: Could not load heros from DB");
		}

		return heroesToBe;
	}

	public List<String> getClassLeaderBoard(int classId)
	{
		List<String> names = new ArrayList<>();
		String query = OlympiadConfig.OLYMPIAD_SHOW_MONTHLY_WINNERS ? (classId == 132 ? GET_EACH_CLASS_LEADER_SOULHOUND : GET_EACH_CLASS_LEADER) : (classId == 132 ? GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND : GET_EACH_CLASS_LEADER_CURRENT);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(query);)
		{
			ps.setInt(1, classId);

			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					names.add(rset.getString("char_name"));
				}
			}
		}
		catch (SQLException var15)
		{
			LOGGER.warning("Olympiad System: Couldn't load olympiad leaders from DB!");
		}

		return names;
	}

	private int getOlympiadTradePoint(int objectId)
	{
		if (this._period == 1 && !NOBLES_RANK.isEmpty())
		{
			if (!NOBLES_RANK.containsKey(objectId))
			{
				return 0;
			}
			StatSet noble = NOBLES.get(objectId);
			if (noble != null && noble.getInt("olympiad_points") != 0)
			{
				int points = !Hero.getInstance().isHero(objectId) && !Hero.getInstance().isUnclaimedHero(objectId) ? 0 : OlympiadConfig.OLYMPIAD_HERO_POINTS;

				points = switch (NOBLES_RANK.get(objectId))
				{
					case 1 -> points + OlympiadConfig.OLYMPIAD_RANK1_POINTS;
					case 2 -> points + OlympiadConfig.OLYMPIAD_RANK2_POINTS;
					case 3 -> points + OlympiadConfig.OLYMPIAD_RANK3_POINTS;
					case 4 -> points + OlympiadConfig.OLYMPIAD_RANK4_POINTS;
					default -> points + OlympiadConfig.OLYMPIAD_RANK5_POINTS;
				} + (this.getCompetitionWon(objectId) > 0 ? 10 : 0);
				noble.set("olympiad_points", 0);
				return points;
			}
			return 0;
		}
		return 0;
	}

	public int getNoblePoints(Player player)
	{
		if (!NOBLES.containsKey(player.getObjectId()))
		{
			StatSet statDat = new StatSet();
			statDat.set("class_id", player.getBaseClass());
			statDat.set("char_name", player.getName());
			statDat.set("olympiad_points", DEFAULT_POINTS);
			statDat.set("competitions_done", 0);
			statDat.set("competitions_won", 0);
			statDat.set("competitions_lost", 0);
			statDat.set("competitions_drawn", 0);
			statDat.set("competitions_done_week", 0);
			statDat.set("to_save", true);
			addNobleStats(player.getObjectId(), statDat);
		}

		return NOBLES.get(player.getObjectId()).getInt("olympiad_points");
	}

	public int getLastNobleOlympiadPoints(int objId)
	{
		int result = 0;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE charId = ?");)
		{
			ps.setInt(1, objId);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getInt(1);
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Could not load last olympiad points:", var14);
		}

		return result;
	}

	public int getCompetitionDone(int objId)
	{
		return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInt("competitions_done");
	}

	public int getCompetitionWon(int objId)
	{
		return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInt("competitions_won");
	}

	public int getCompetitionLost(int objId)
	{
		return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInt("competitions_lost");
	}

	public int getCompetitionDoneWeek(int objId)
	{
		return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInt("competitions_done_week");
	}

	public int getRemainingWeeklyMatches(int objId)
	{
		return Math.max(OlympiadConfig.OLYMPIAD_MAX_WEEKLY_MATCHES - this.getCompetitionDoneWeek(objId), 0);
	}

	protected void deleteNobles()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("TRUNCATE olympiad_nobles");)
		{
			statement.execute();
		}
		catch (SQLException var9)
		{
			LOGGER.warning("Olympiad System: Couldn't delete nobles from DB!");
		}

		NOBLES.clear();
	}

	public static StatSet addNobleStats(int charId, StatSet data)
	{
		return NOBLES.put(charId, data);
	}

	public static Olympiad getInstance()
	{
		return Olympiad.SingletonHolder.INSTANCE;
	}

	protected class OlympiadEndTask implements Runnable
	{
		protected OlympiadEndTask()
		{
			Objects.requireNonNull(Olympiad.this);
			super();
		}

		@Override
		public void run()
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.ROUND_S1_OF_THE_OLYMPIAD_HAS_NOW_ENDED);
			sm.addInt(Olympiad.this._currentCycle);
			Broadcast.toAllOnlinePlayers(sm);
			if (Olympiad.this._scheduledWeeklyTask != null)
			{
				Olympiad.this._scheduledWeeklyTask.cancel(true);
			}

			Olympiad.this.saveNobleData();
			Olympiad.this._period = 1;
			List<StatSet> heroesToBe = Olympiad.this.sortHerosToBe();
			Hero.getInstance().resetData();
			Hero.getInstance().computeNewHeroes(heroesToBe);
			Olympiad.this.saveOlympiadStatus();
			Olympiad.this.updateMonthlyData();
			Calendar validationEnd = Calendar.getInstance();
			Olympiad.this._validationEnd = validationEnd.getTimeInMillis() + Olympiad.VALIDATION_PERIOD;
			Olympiad.this.loadNoblesRank();
			Olympiad.this._scheduledValdationTask = ThreadPool.schedule(Olympiad.this.new ValidationEndTask(), Olympiad.this.getMillisToValidationEnd());
		}
	}

	private static class SingletonHolder
	{
		protected static final Olympiad INSTANCE = new Olympiad();
	}

	protected class ValidationEndTask implements Runnable
	{
		protected ValidationEndTask()
		{
			Objects.requireNonNull(Olympiad.this);
			super();
		}

		@Override
		public void run()
		{
			Broadcast.toAllOnlinePlayers("Olympiad Validation Period has ended");
			Olympiad.this._period = 0;
			Olympiad.this._currentCycle++;
			Olympiad.this.deleteNobles();
			Olympiad.this.setNewOlympiadEnd();
			Olympiad.this.init();
		}
	}
}
