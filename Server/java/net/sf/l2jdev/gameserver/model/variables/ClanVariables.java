package net.sf.l2jdev.gameserver.model.variables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;

public class ClanVariables extends AbstractVariables
{
	private static final Logger LOGGER = Logger.getLogger(ClanVariables.class.getName());
	public static final String CONTRIBUTION = "CONTRIBUTION_";
	public static final String CONTRIBUTION_WEEKLY = "CONTRIBUTION_WEEKLY_";
	private final AtomicBoolean _scheduledSave = new AtomicBoolean(false);
	private final int _objectId;
	
	public ClanVariables(int objectId)
	{
		this._objectId = objectId;
		this.restoreMe();
	}
	
	public boolean restoreMe()
	{
		this.clearChangeTracking();
		
		label137:
		{
			boolean st;
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stx = con.prepareStatement("SELECT * FROM clan_variables WHERE clanId = ?");)
			{
				stx.setInt(1, this._objectId);
				
				try (ResultSet rset = stx.executeQuery())
				{
					while (rset.next())
					{
						this.set(rset.getString("var"), rset.getString("val"), false);
					}
					break label137;
				}
			}
			catch (SQLException var21)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not restore variables for: " + this._objectId, var21);
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
				try (PreparedStatement stx = con.prepareStatement("DELETE FROM clan_variables WHERE clanId = ? AND var = ?"))
				{
					for (String name : this._deleted)
					{
						stx.setInt(1, this._objectId);
						stx.setString(2, name);
						stx.addBatch();
					}
					
					stx.executeBatch();
				}
			}
			
			if (!this._added.isEmpty())
			{
				try (PreparedStatement stx = con.prepareStatement("INSERT INTO clan_variables (clanId, var, val) VALUES (?, ?, ?)"))
				{
					for (String name : this._added)
					{
						Object value = this.getSet().get(name);
						if (value != null)
						{
							stx.setInt(1, this._objectId);
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
				try (PreparedStatement stx = con.prepareStatement("UPDATE clan_variables SET val = ? WHERE clanId = ? AND var = ?"))
				{
					for (String namex : this._modified)
					{
						Object value = this.getSet().get(namex);
						if (value != null)
						{
							stx.setString(1, String.valueOf(value));
							stx.setInt(2, this._objectId);
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
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not update variables for: " + this._objectId, var25);
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
		
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("DELETE FROM clan_variables WHERE clanId = ?");)
		{
			st.setInt(1, this._objectId);
			st.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not delete variables for: " + this._objectId, var9);
			this._saveLock.unlock();
			return false;
		}
		
		this.getSet().clear();
		this.clearChangeTracking();
		this._saveLock.unlock();
		return true;
	}
	
	public boolean deleteWeeklyContribution()
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement st = con.prepareStatement("DELETE FROM clan_variables WHERE var LIKE 'CONTRIBUTION_WEEKLY_%' AND clanId = ?"))
				{
					st.setInt(1, this._objectId);
					st.execute();
				}
				
				this.getSet().entrySet().stream().filter(it -> it.getKey().startsWith("CONTRIBUTION_WEEKLY_")).collect(Collectors.toList()).forEach(it -> this.remove(it.getKey()));
			}
			
			return true;
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not delete variables for: " + this._objectId, var9);
			return false;
		}
	}
}
