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

public class ItemVariables extends AbstractVariables
{
	private static final Logger LOGGER = Logger.getLogger(ItemVariables.class.getName());
	 
	public static final String VISUAL_ID = "visualId";
	public static final String VISUAL_APPEARANCE_STONE_ID = "visualAppearanceStoneId";
	public static final String VISUAL_APPEARANCE_LIFE_TIME = "visualAppearanceLifetime";
	public static final String TRANSMOG_ID = "transmogId";
	public static final String BLESSED = "blessed";
	private final AtomicBoolean _scheduledSave = new AtomicBoolean(false);
	private final int _objectId;

	public ItemVariables(int objectId)
	{
		this._objectId = objectId;
		this.restoreMe();
	}

	public static boolean hasVariables(int objectId)
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM item_variables WHERE id = ?");)
			{
				st.setInt(1, objectId);

				try (ResultSet rset = st.executeQuery())
				{
					if (rset.next())
					{
						return rset.getInt(1) > 0;
					}
				}
			}

			return true;
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.WARNING, ItemVariables.class.getSimpleName() + ": Could not select variables count for: " + objectId, var12);
			return false;
		}
	}

	public boolean restoreMe()
	{
		this.clearChangeTracking();

		label137:
		{
			boolean st;
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stx = con.prepareStatement("SELECT * FROM item_variables WHERE id = ?");)
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
				try (PreparedStatement stx = con.prepareStatement("DELETE FROM item_variables WHERE id = ? AND var = ?"))
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
				try (PreparedStatement stx = con.prepareStatement("INSERT INTO item_variables (id, var, val) VALUES (?, ?, ?)"))
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
				try (PreparedStatement stx = con.prepareStatement("UPDATE item_variables SET val = ? WHERE id = ? AND var = ?"))
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

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("DELETE FROM item_variables WHERE id = ?");)
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
}
