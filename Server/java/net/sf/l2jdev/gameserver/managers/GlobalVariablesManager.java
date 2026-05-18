package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.variables.AbstractVariables;

public class GlobalVariablesManager extends AbstractVariables
{
	private static final Logger LOGGER = Logger.getLogger(GlobalVariablesManager.class.getName());
	public static final String SELECT_QUERY = "SELECT * FROM global_variables";
	public static final String DELETE_QUERY = "DELETE FROM global_variables";
	public static final String INSERT_QUERY = "REPLACE INTO global_variables (var, value) VALUES (?, ?)";
	public static final String DAILY_TASK_RESET = "DAILY_TASK_RESET";
	public static final String IS_EVEN_WEEK = "IS_EVEN_WEEK";
	public static final String NEXT_EVEN_WEEK_SWAP = "NEXT_EVEN_WEEK_SWAP";
	public static final String MONSTER_ARENA_VARIABLE = "MA_C";
	public static final String RANKING_POWER_COOLDOWN = "RANKING_POWER_COOLDOWN";
	public static final String RANKING_POWER_LOCATION = "RANKING_POWER_LOCATION";
	public static final String PURGE_REWARD_TIME = "PURGE_REWARD_TIME";
	public static final String BALOK_REMAIN_TIME = "BALOK_REMAIN_TIME";

	protected GlobalVariablesManager()
	{
		this.restoreMe();
	}

	public boolean restoreMe()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement st = con.createStatement(); ResultSet rset = st.executeQuery("SELECT * FROM global_variables");)
		{
			while (rset.next())
			{
				this.set(rset.getString("var"), rset.getString("value"));
			}
		}
		catch (SQLException var12)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Couldn't restore global variables.");
			return false;
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this.getSet().size() + " variables.");
		return true;
	}

	public boolean storeMe()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement del = con.createStatement(); PreparedStatement st = con.prepareStatement("REPLACE INTO global_variables (var, value) VALUES (?, ?)");)
		{
			del.execute("DELETE FROM global_variables");

			for (Entry<String, Object> entry : this.getSet().entrySet())
			{
				st.setString(1, entry.getKey());
				st.setString(2, String.valueOf(entry.getValue()));
				st.addBatch();
			}

			st.executeBatch();
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't save global variables to database.", var12);
			return false;
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Stored " + this.getSet().size() + " variables.");
		return true;
	}

	public boolean deleteMe()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); Statement del = con.createStatement();)
			{
				del.execute("DELETE FROM global_variables");
			}

			return true;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't delete global variables to database.", var9);
			return false;
		}
	}

	public static GlobalVariablesManager getInstance()
	{
		return GlobalVariablesManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final GlobalVariablesManager INSTANCE = new GlobalVariablesManager();
	}
}
