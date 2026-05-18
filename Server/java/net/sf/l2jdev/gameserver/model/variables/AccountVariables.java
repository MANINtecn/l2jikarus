package net.sf.l2jdev.gameserver.model.variables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;

public class AccountVariables extends AbstractVariables
{
	private static final Logger LOGGER = Logger.getLogger(AccountVariables.class.getName());
	
	public static final String HWID = "HWID";
	public static final String HWIDSLIT_VAR = "\t";
	public static final String PRIME_SHOP_PRODUCT_COUNT = "PSPCount";
	public static final String PRIME_SHOP_PRODUCT_DAILY_COUNT = "PSPDailyCount";
	public static final String LCOIN_SHOP_PRODUCT_COUNT = "LCSCount";
	public static final String LCOIN_SHOP_PRODUCT_DAILY_COUNT = "LCSDailyCount";
	public static final String LCOIN_SHOP_PRODUCT_WEEKLY_COUNT = "LCSWeeklyCount";
	public static final String LCOIN_SHOP_PRODUCT_MONTHLY_COUNT = "LCSMonthlyCount";
	public static final String VIP_POINTS = "VipPoints";
	public static final String VIP_TIER = "VipTier";
	public static final String VIP_EXPIRATION = "VipExpiration";
	public static final String VIP_ITEM_BOUGHT = "Vip_Item_Bought";
	public static final String A_GRADE_RELIC_ATEMPTS = "A_GRADE_RELIC_ATEMPTS";
	public static final String B_GRADE_RELIC_ATEMPTS = "B_GRADE_RELIC_ATEMPTS";
	public static final String UNCONFIRMED_RELICS_COUNT = "UNCONFIRMED_RELICS_COUNT";
	private final AtomicBoolean _scheduledSave = new AtomicBoolean(false);
	private final String _accountName;

	public AccountVariables(String accountName)
	{
		this._accountName = accountName;
		this.restoreMe();
	}

	public boolean restoreMe()
	{
		this.clearChangeTracking();

		label137:
		{
			boolean st;
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stx = con.prepareStatement("SELECT * FROM account_gsdata WHERE account_name = ?");)
			{
				stx.setString(1, this._accountName);

				try (ResultSet rset = stx.executeQuery())
				{
					while (rset.next())
					{
						this.set(rset.getString("var"), rset.getString("value"), false);
					}
					break label137;
				}
			}
			catch (SQLException var21)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not restore variables for: " + this._accountName, var21);
				st = false;
			}
			finally
			{
				this.compareAndSetChanges(true, false);
			}

			return st;
		}

		return true;
	}

	public boolean storeMe()
	{
		if (!this.hasChanges())
		{
			return false;
		}
		else if (!this._scheduledSave.get())
		{
			this._scheduledSave.set(true);
			ThreadPool.schedule(() -> {
				this._scheduledSave.set(false);
				this.saveNow();
			}, 60000L);
			return true;
		}
		else
		{
			return this.saveNow();
		}
	}

	public boolean saveNow()
	{
		return !this.hasChanges() ? false : this.saveNowSync();
	}

	private boolean saveNowSync()
	{
		this._saveLock.lock();

		boolean st;
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (!this._deleted.isEmpty())
			{
				try (PreparedStatement stx = con.prepareStatement("DELETE FROM account_gsdata WHERE account_name = ? AND var = ?"))
				{
					for (String name : this._deleted)
					{
						stx.setString(1, this._accountName);
						stx.setString(2, name);
						stx.addBatch();
					}

					stx.executeBatch();
				}
			}

			if (!this._added.isEmpty())
			{
				try (PreparedStatement stx = con.prepareStatement("INSERT INTO account_gsdata (account_name, var, value) VALUES (?, ?, ?)"))
				{
					for (String name : this._added)
					{
						Object value = this.getSet().get(name);
						if (value != null)
						{
							stx.setString(1, this._accountName);
							stx.setString(2, name);
							stx.setString(3, String.valueOf(value));
							stx.addBatch();
						}
					}

					stx.executeBatch();
				}
			}

			if (!this._modified.isEmpty())
			{
				try (PreparedStatement stx = con.prepareStatement("UPDATE account_gsdata SET value = ? WHERE account_name = ? AND var = ?"))
				{
					for (String namex : this._modified)
					{
						Object value = this.getSet().get(namex);
						if (value != null)
						{
							stx.setString(1, String.valueOf(value));
							stx.setString(2, this._accountName);
							stx.setString(3, namex);
							stx.addBatch();
						}
					}

					stx.executeBatch();
				}
			}

			return true;
		}
		catch (SQLException var25)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not update variables for: " + this._accountName, var25);
			this._saveLock.unlock();
			st = false;
		}
		finally
		{
			this.clearChangeTracking();
			this.compareAndSetChanges(true, false);
			this._saveLock.unlock();
		}

		return st;
	}

	public boolean deleteMe()
	{
		this._saveLock.lock();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("DELETE FROM account_gsdata WHERE account_name = ?");)
		{
			st.setString(1, this._accountName);
			st.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not delete variables for: " + this._accountName, var9);
			this._saveLock.unlock();
			return false;
		}

		this.getSet().clear();
		this.clearChangeTracking();
		this._saveLock.unlock();
		return true;
	}

	public static boolean deleteVipPurchases(String var)
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ?");)
			{
				st.setString(1, var);
				st.execute();
			}

			return true;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, "AccountVariables: Could not delete vip variables!", var9);
			return false;
		}
	}
}
