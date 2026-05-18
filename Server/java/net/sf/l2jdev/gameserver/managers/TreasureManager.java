package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.siege.Castle;

public class TreasureManager
{
	private static final Logger LOGGER = Logger.getLogger(TreasureManager.class.getName());
	public static final String TREASURE_MANAGER_NEXT_RUN_VAR = "TREASURE_MANAGER_NEXT_RUN";

	public TreasureManager()
	{
		this.nextDate();
	}

	private void updateTreasure()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM castle"); ResultSet rs = ps.executeQuery();)
		{
			while (rs.next())
			{
				int treasure = rs.getInt("dynamicTreasury");
				int castleId = rs.getInt("id");
				if (treasure > 0)
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleId);
					castle.addToTreasuryNoTax(treasure);
				}
			}
		}
		catch (Exception var18)
		{
			LOGGER.log(Level.WARNING, var18.getMessage(), var18);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle SET dynamicTreasury = ?");)
		{
			ps.setLong(1, 0L);
			ps.execute();
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, var14.getMessage(), var14);
		}

		this.nextDate();
	}

	private void nextDate()
	{
		Calendar calendar = Calendar.getInstance();
		long nextRun = GlobalVariablesManager.getInstance().getLong("TREASURE_MANAGER_NEXT_RUN", 0L);
		boolean lastRun = nextRun > System.currentTimeMillis();
		int weekday = calendar.get(7);
		calendar.set(7, 2);
		calendar.set(11, 12);
		calendar.set(12, 0);
		calendar.set(13, 0);
		if (!lastRun)
		{
			int days = 2 - weekday;
			if (days <= 0)
			{
				days += 7;
			}

			calendar.add(6, days);
			GlobalVariablesManager.getInstance().set("TREASURE_MANAGER_NEXT_RUN", calendar.getTimeInMillis());
			ThreadPool.schedule(this::updateTreasure, 15000L);
		}
		else
		{
			long next = GlobalVariablesManager.getInstance().getLong("TREASURE_MANAGER_NEXT_RUN");
			ThreadPool.schedule(this::updateTreasure, next - System.currentTimeMillis());
		}
	}

	public static TreasureManager getInstance()
	{
		return TreasureManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final TreasureManager INSTANCE = new TreasureManager();
	}
}
