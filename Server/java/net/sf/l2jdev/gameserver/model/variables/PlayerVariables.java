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

public class PlayerVariables extends AbstractVariables
{
	private static final Logger LOGGER = Logger.getLogger(PlayerVariables.class.getName());
	 
	public static final String INSTANCE_ORIGIN = "INSTANCE_ORIGIN";
	public static final String INSTANCE_RESTORE = "INSTANCE_RESTORE";
	public static final String RESTORE_LOCATION = "RESTORE_LOCATION";
	public static final String HAIR_ACCESSORY_VARIABLE_NAME = "HAIR_ACCESSORY_ENABLED";
	public static final String WORLD_CHAT_VARIABLE_NAME = "WORLD_CHAT_USED";
	public static final String VITALITY_ITEMS_USED_VARIABLE_NAME = "VITALITY_ITEMS_USED";
	public static final String UI_KEY_MAPPING = "UI_KEY_MAPPING";
	public static final String CLIENT_SETTINGS = "CLIENT_SETTINGS";
	public static final String ATTENDANCE_DATE = "ATTENDANCE_DATE";
	public static final String ATTENDANCE_INDEX = "ATTENDANCE_INDEX";
	public static final String ABILITY_POINTS_MAIN_CLASS = "ABILITY_POINTS";
	public static final String ABILITY_POINTS_DUAL_CLASS = "ABILITY_POINTS_DUAL_CLASS";
	public static final String ABILITY_POINTS_USED_MAIN_CLASS = "ABILITY_POINTS_USED";
	public static final String ABILITY_POINTS_USED_DUAL_CLASS = "ABILITY_POINTS_DUAL_CLASS_USED";
	public static final String REVELATION_SKILL_1_MAIN_CLASS = "RevelationSkill1";
	public static final String REVELATION_SKILL_2_MAIN_CLASS = "RevelationSkill2";
	public static final String REVELATION_SKILL_1_DUAL_CLASS = "DualclassRevelationSkill1";
	public static final String REVELATION_SKILL_2_DUAL_CLASS = "DualclassRevelationSkill2";
	public static final String LAST_PLEDGE_REPUTATION_LEVEL = "LAST_PLEDGE_REPUTATION_LEVEL";
	public static final String FORTUNE_TELLING_VARIABLE = "FortuneTelling";
	public static final String FORTUNE_TELLING_BLACK_CAT_VARIABLE = "FortuneTellingBlackCat";
	public static final String DELUSION_RETURN = "DELUSION_RETURN";
	public static final String BALTHUS_REWARD = "BALTHUS_REWARD";
	public static final String BALTHUS_BAG = "BALTHUS_BAG";
	public static final String AUTO_USE_SETTINGS = "AUTO_USE_SETTINGS";
	public static final String AUTO_USE_SHORTCUTS = "AUTO_USE_SHORTCUTS";
	public static final String LAST_HUNTING_ZONE_ID = "LAST_HUNTING_ZONE_ID";
	public static final String HUNTING_ZONE_ENTRY = "HUNTING_ZONE_ENTRY_";
	public static final String HUNTING_ZONE_TIME = "HUNTING_ZONE_TIME_";
	public static final String HUNTING_ZONE_REMAIN_REFILL = "HUNTING_ZONE_REMAIN_REFILL_";
	public static final String SAYHA_GRACE_SUPPORT_ENDTIME = "SAYHA_GRACE_SUPPORT_ENDTIME";
	public static final String LIMITED_SAYHA_GRACE_ENDTIME = "LIMITED_SAYHA_GRACE_ENDTIME";
	public static final String MAGIC_LAMP_EXP = "MAGIC_LAMP_EXP";
	public static final String DEATH_POINT_COUNT = "DEATH_POINT_COUNT";
	public static final String BEAST_POINT_COUNT = "BEAST_POINT_COUNT";
	public static final String ASSASSINATION_POINT_COUNT = "ASSASSINATION_POINT_COUNT";
	public static final String LIGHT_POINT_COUNT = "LIGHT_POINT_COUNT";
	public static final String WOLF_POINT_COUNT = "WOLF_POINT_COUNT";
	public static final String FAVORITE_TELEPORTS = "FAVORITE_TELEPORTS";
	public static final String ELIXIRS_AVAILABLE = "ELIXIRS_AVAILABLE";
	public static final String STAT_POINTS = "STAT_POINTS";
	public static final String STAT_STR = "STAT_STR";
	public static final String STAT_DEX = "STAT_DEX";
	public static final String STAT_CON = "STAT_CON";
	public static final String STAT_INT = "STAT_INT";
	public static final String STAT_WIT = "STAT_WIT";
	public static final String STAT_MEN = "STAT_MEN";
	public static final String RESURRECT_BY_PAYMENT_COUNT = "RESURRECT_BY_PAYMENT_COUNT";
	public static final String PURGE_LAST_CATEGORY = "PURGE_LAST_CATEGORY";
	public static final String CLAN_JOIN_TIME = "CLAN_JOIN_TIME";
	public static final String CLAN_DONATION_POINTS = "CLAN_DONATION_POINTS";
	public static final String HENNA1_DURATION = "HENNA1_DURATION";
	public static final String HENNA2_DURATION = "HENNA2_DURATION";
	public static final String HENNA3_DURATION = "HENNA3_DURATION";
	public static final String HENNA4_DURATION = "HENNA4_DURATION";
	public static final String DYE_POTENTIAL_DAILY_STEP = "DYE_POTENTIAL_DAILY_STEP";
	public static final String DYE_POTENTIAL_DAILY_COUNT = "DYE_POTENTIAL_DAILY_COUNT";
	public static final String DYE_POTENTIAL_DAILY_COUNT_ENCHANT_RESET = "DYE_POTENTIAL_DAILY_COUNT_ENCHANT_RESET";
	public static final String MISSION_LEVEL_PROGRESS = "MISSION_LEVEL_PROGRESS_";
	public static final String BALOK_AVAILABLE_REWARD = "BALOK_AVAILABLE_REWARD";
	public static final String PRISON_WAIT_TIME = "PRISON_WAIT_TIME";
	public static final String PRISON_2_POINTS = "PRISON_2_POINTS";
	public static final String PRISON_3_POINTS = "PRISON_3_POINTS";
	public static final String DUAL_INVENTORY_SLOT = "DUAL_INVENTORY_SLOT";
	public static final String DUAL_INVENTORY_SET_A = "DUAL_INVENTORY_SET_A";
	public static final String DUAL_INVENTORY_SET_B = "DUAL_INVENTORY_SET_B";
	public static final String DAILY_EXTRACT_ITEM = "DAILY_EXTRACT_ITEM";
	public static final String SKILL_ENCHANT_STAR = "SKILL_ENCHANT_STAR_";
	public static final String SKILL_TRY_ENCHANT = "SKILL_TRY_ENCHANT_";
	public static final String CROSS_EVENT_DAILY_RESET_COUNT = "CROSS_EVENT_DAILY_RESET_COUNT";
	public static final String CROSS_EVENT_CELLS = "CROSS_EVENT_CELLS";
	public static final String CROSS_EVENT_REWARDS = "CROSS_EVENT_REWARDS";
	public static final String CROSS_EVENT_ADVANCED_COUNT = "CROSS_EVENT_ADVANCED_COUNT";
	public static final String CHAT_BACKGROUND_BLUE = "CHAT_BACKGROUND_BLUE";
	public static final String CHAT_BACKGROUND_YELLOW = "CHAT_BACKGROUND_YELLOW";
	public static final String ENABLE_CHAT_BACKGROUND = "ENABLE_CHAT_BACKGROUND";
	public static final String ACTIVE_CHAT_BACKGROUND = "ACTIVE_CHAT_BACKGROUND";
	public static final String ACTIVE_RELIC = "ACTIVE_RELIC";
	public static final String SUMMONED_GUARDIAN_NPC_IDS = "SUMMONED_GUARDIAN_NPC_IDS";
	public static final String AVAILABLE_CHARACTER_STYLES = "AVAILABLE_CHARACTER_STYLES_";
	public static final String FAVORITE_CHARACTER_STYLES = "FAVORITE_CHARACTER_STYLES_";
	public static final String ACTIVE_CHARACTER_STYLE = "ACTIVE_CHARACTER_STYLE_";
	public static final String FIRST_LOGIN_BUFF = "FIRST_LOGIN_BUFF";
	private final AtomicBoolean _scheduledSave = new AtomicBoolean(false);
	private final int _objectId;

	public PlayerVariables(int objectId)
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
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stx = con.prepareStatement("SELECT * FROM character_variables WHERE charId = ?");)
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
				try (PreparedStatement stx = con.prepareStatement("DELETE FROM character_variables WHERE charId = ? AND var = ?"))
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
				try (PreparedStatement stx = con.prepareStatement("INSERT INTO character_variables (charId, var, val) VALUES (?, ?, ?)"))
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
				try (PreparedStatement stx = con.prepareStatement("UPDATE character_variables SET val = ? WHERE charId = ? AND var = ?"))
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

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("DELETE FROM character_variables WHERE charId = ?");)
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
