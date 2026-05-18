package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.RankingHistoryDataHolder;

public class RankingHistory
{
	private static final Logger LOGGER = Logger.getLogger(RankingHistory.class.getName());
	public static final int NUM_HISTORY_DAYS = 7;
	private final Player _player;
	private final Collection<RankingHistoryDataHolder> _data = new ArrayList<>();
	private long _nextUpdate = 0L;

	public RankingHistory(Player player)
	{
		this._player = player;
	}

	public void store()
	{
		int ranking = RankManager.getInstance().getPlayerGlobalRank(this._player);
		long exp = this._player.getExp();
		int today = (int) (System.currentTimeMillis() / 86400000L);
		int oldestDay = today - 7 + 1;

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_ranking_history (charId, day, ranking, exp) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE ranking = ?, exp = ?");
			PreparedStatement deleteSt = con.prepareStatement("DELETE FROM character_ranking_history WHERE charId = ? AND day < ?");)
		{
			statement.setInt(1, this._player.getObjectId());
			statement.setInt(2, today);
			statement.setInt(3, ranking);
			statement.setLong(4, exp);
			statement.setInt(5, ranking);
			statement.setLong(6, exp);
			statement.execute();
			deleteSt.setInt(1, this._player.getObjectId());
			deleteSt.setInt(2, oldestDay);
			deleteSt.execute();
		}
		catch (Exception var17)
		{
			LOGGER.log(Level.WARNING, "Could not insert RankingCharHistory data: " + var17.getMessage(), var17);
		}
	}

	public Collection<RankingHistoryDataHolder> getData()
	{
		long currentTime = System.currentTimeMillis();
		if (currentTime > this._nextUpdate)
		{
			this._data.clear();
			if (this._nextUpdate == 0L)
			{
				this.store();
			}

			this._nextUpdate = currentTime + GeneralConfig.CHAR_DATA_STORE_INTERVAL;

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM character_ranking_history WHERE charId = ? ORDER BY day DESC");)
			{
				statement.setInt(1, this._player.getObjectId());

				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int day = rset.getInt("day");
						long timestamp = day * 86400000L + 86400000L;
						int ranking = rset.getInt("ranking");
						long exp = rset.getLong("exp");
						this._data.add(new RankingHistoryDataHolder(timestamp / 1000L, ranking, exp));
					}
				}
			}
			catch (Exception var18)
			{
				LOGGER.log(Level.WARNING, "Could not get RankingCharHistory data: " + var18.getMessage(), var18);
			}
		}

		return this._data;
	}
}
