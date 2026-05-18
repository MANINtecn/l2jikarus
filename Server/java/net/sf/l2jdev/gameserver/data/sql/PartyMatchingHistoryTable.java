package net.sf.l2jdev.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomHistory;

public class PartyMatchingHistoryTable
{
	private static final Logger LOGGER = Logger.getLogger(PartyMatchingHistoryTable.class.getName());
	public static final String RESTORE_PARTY_HISTORY = "SELECT title, leader FROM party_matching_history ORDER BY id DESC LIMIT 100";
	public static final String DELETE_PARTY_HISTORY = "DELETE FROM party_matching_history";
	public static final String INSERT_PARTY_HISTORY = "INSERT INTO party_matching_history (title,leader) values (?,?)";
	private static final LinkedList<MatchingRoomHistory> HISTORY = new LinkedList<>();

	protected PartyMatchingHistoryTable()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT title, leader FROM party_matching_history ORDER BY id DESC LIMIT 100"); ResultSet rset = statement.executeQuery();)
		{
			while (rset.next())
			{
				this.addRoomHistory(rset.getString("title"), rset.getString("leader"));
			}
		}
		catch (Exception var12)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Could not load data: " + var12.getMessage());
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + HISTORY.size() + " party matching history data.");
	}

	public void addRoomHistory(String title, String leaderName)
	{
		synchronized (HISTORY)
		{
			HISTORY.add(new MatchingRoomHistory(title, leaderName));
			if (HISTORY.size() > 100)
			{
				HISTORY.removeFirst();
			}
		}
	}

	public Collection<MatchingRoomHistory> getHistory()
	{
		return HISTORY;
	}

	public void storeMe()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps1 = con.prepareStatement("DELETE FROM party_matching_history"); PreparedStatement ps2 = con.prepareStatement("INSERT INTO party_matching_history (title,leader) values (?,?)");)
		{
			ps1.execute();

			for (MatchingRoomHistory history : HISTORY)
			{
				ps2.setString(1, history.getTitle());
				ps2.setString(2, history.getLeader());
				ps2.addBatch();
			}

			ps2.executeBatch();
		}
		catch (Exception var12)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": PartyMatchingHistoryTable: Problem inserting room history!");
		}
	}

	public static PartyMatchingHistoryTable getInstance()
	{
		return PartyMatchingHistoryTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PartyMatchingHistoryTable INSTANCE = new PartyMatchingHistoryTable();
	}
}
