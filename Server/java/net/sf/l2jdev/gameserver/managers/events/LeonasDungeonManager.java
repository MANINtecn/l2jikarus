package net.sf.l2jdev.gameserver.managers.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class LeonasDungeonManager
{
	private static final Logger LOGGER = Logger.getLogger(LeonasDungeonManager.class.getName());
	public static final String INSERT_DUNGEON_RANKING = "REPLACE INTO leonas_dungeon_ranking (charId, points) VALUES (?, ?)";
	public static final String SELECT_DUNGEON_RANKING = "SELECT charId, points FROM leonas_dungeon_ranking";
	public static final String DELETE_DUNGEON_RANKING = "DELETE FROM leonas_dungeon_ranking WHERE charId=?";
	public static final String DELETE_ALL_DUNGEON_RANKING = "DELETE FROM leonas_dungeon_ranking";
	private final ConcurrentHashMap<Integer, AtomicInteger> _playerPoints = new ConcurrentHashMap<>();
	private final List<Integer> _rewardedPlayers = new ArrayList<>();

	public LeonasDungeonManager()
	{
		this.restoreDungeonRankingFromDatabase();
		this.scheduleMondayReset();
	}

	private void scheduleMondayReset()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(7, 2);
		calendar.set(11, 0);
		calendar.set(12, 0);
		calendar.set(13, 0);
		calendar.set(14, 0);
		if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			calendar.add(4, 1);
		}

		ThreadPool.schedule(() -> {
			getInstance().rewardTopPlayersOnMonday();
			getInstance().deleteAllDungeonRankingData();
			LOGGER.info(this.getClass().getSimpleName() + ": Leona Dungeon reset");
			this.scheduleMondayReset();
		}, calendar.getTimeInMillis() - System.currentTimeMillis());
	}

	public void addPointsForPlayer(Player player, int val)
	{
		this._playerPoints.computeIfAbsent(player.getObjectId(), _ -> new AtomicInteger()).addAndGet(val);
		this.saveDungeonRankingToDatabase();
	}

	public long getTotalPoints()
	{
		return this._playerPoints.values().stream().mapToLong(AtomicInteger::get).sum();
	}

	public Map<Integer, Integer> getTopPlayers(int count)
	{
		return this._playerPoints.entrySet().stream().sorted(Entry.<Integer, AtomicInteger> comparingByValue(Comparator.comparingInt(AtomicInteger::get).reversed()).thenComparing(Entry.comparingByKey())).limit(count).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().get(), (e1, _) -> e1, LinkedHashMap::new));
	}

	public int getPlayerRank(Player player)
	{
		int playerPoints = this._playerPoints.getOrDefault(player.getObjectId(), new AtomicInteger(0)).get();
		return (int) this._playerPoints.values().stream().filter(points -> points.get() > playerPoints).count() + 1;
	}

	public int getPlayerPoints(Player player)
	{
		return this._playerPoints.getOrDefault(player.getObjectId(), new AtomicInteger(0)).get();
	}

	public void rewardTopPlayersOnMonday()
	{
		int rank = 1;
		Map<Integer, Integer> topPlayers = this.getTopPlayers(150);

		for (Entry<Integer, Integer> entry : topPlayers.entrySet())
		{
			int playerId = entry.getKey();
			int rewardsId = 99845;
			int rewards = 1;
			if (rank == 1)
			{
				rewards = 10;
				rewardsId = 97366;
			}
			else if (rank == 2 || rank == 3)
			{
				rewards = 1;
				rewardsId = 99849;
			}
			else if (rank >= 4 && rank <= 10)
			{
				rewards = 1;
				rewardsId = 99848;
			}
			else if (rank >= 11 && rank <= 50)
			{
				rewards = 1;
				rewardsId = 99847;
			}
			else if (rank >= 51 && rank <= 100)
			{
				rewards = 1;
				rewardsId = 99846;
			}
			else if (rank >= 101 && rank <= 150)
			{
				rewards = 1;
				rewardsId = 99845;
			}

			this.giveRewardToPlayer(playerId, rewardsId, rewards);
			rank++;
		}
	}

	public void giveRewardToPlayer(int playerId, int item, int rewards)
	{
		ItemHolder holder = new ItemHolder(item, rewards);
		Message message = new Message(-1, playerId, false, "Weekly Leona Dungeon Reward", "Congratulations! Here are your rewards.", 0L);
		message.createAttachments();
		message.getAttachments().addItem(ItemProcessType.REWARD, holder.getId(), holder.getCount(), null, null);
		MailManager.getInstance().sendMessage(message);
	}

	public void clear()
	{
		this._playerPoints.clear();
		this._rewardedPlayers.clear();
	}

	public void saveDungeonRankingToDatabase()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO leonas_dungeon_ranking (charId, points) VALUES (?, ?)");)
		{
			this._playerPoints.forEach((playerId, points) -> {
				try
				{
					statement.setInt(1, playerId);
					statement.setInt(2, points.get());
					statement.addBatch();
				}
				catch (SQLException var5)
				{
					LOGGER.severe(this.getClass().getSimpleName() + ": Error preparing batch statement: " + var5.getMessage());
				}
			});
			statement.executeBatch();
		}
		catch (SQLException var9)
		{
			LOGGER.severe(this.getClass().getSimpleName() + ": Error during batch database operation: " + var9.getMessage());
		}
	}

	public void restoreDungeonRankingFromDatabase()
	{
		this._playerPoints.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT charId, points FROM leonas_dungeon_ranking"); ResultSet result = statement.executeQuery();)
		{
			while (result.next())
			{
				int playerId = result.getInt("charId");
				int points = result.getInt("points");
				this._playerPoints.put(playerId, new AtomicInteger(points));
			}
		}
		catch (SQLException var12)
		{
			LOGGER.severe(this.getClass().getSimpleName() + ": Error restoring Leona Dungeon data from database: " + var12.getMessage());
		}
	}

	public void deletePlayerFromDungeonRanking(int playerId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM leonas_dungeon_ranking WHERE charId=?");)
		{
			statement.setInt(1, playerId);
			statement.executeUpdate();
		}
		catch (SQLException var10)
		{
			LOGGER.severe(this.getClass().getSimpleName() + ": Error deleting Leona Dungeon player from database: " + var10.getMessage());
		}

		this._playerPoints.remove(playerId);
	}

	public void deleteAllDungeonRankingData()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM leonas_dungeon_ranking");)
		{
			statement.executeUpdate();
		}
		catch (SQLException var9)
		{
			LOGGER.severe(this.getClass().getSimpleName() + ": Error deleting all Leona dungeon ranking data from database: " + var9.getMessage());
		}

		this.clear();
	}

	public static LeonasDungeonManager getInstance()
	{
		return LeonasDungeonManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final LeonasDungeonManager INSTANCE = new LeonasDungeonManager();
	}
}
