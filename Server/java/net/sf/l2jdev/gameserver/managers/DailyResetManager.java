package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.time.TimeUtil;
import net.sf.l2jdev.gameserver.config.AttendanceRewardsConfig;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.HuntPassConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.TrainingCampConfig;
import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.data.holders.LimitShopProductHolder;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.DailyMissionData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopCraftData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopData;
import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.data.xml.PrimeShopData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.TimedHuntingZoneData;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.SubClassHolder;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.OnDailyReset;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.primeshop.PrimeShopGroup;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.variables.AccountVariables;
import net.sf.l2jdev.gameserver.model.vip.VipManager;
import net.sf.l2jdev.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExWorldChatCnt;

public class DailyResetManager
{
	private static final Logger LOGGER = Logger.getLogger(DailyResetManager.class.getName());
	private static final Set<Integer> RESET_SKILLS = new HashSet<>();
	public static final Set<Integer> RESET_ITEMS = new HashSet<>();

	protected DailyResetManager()
	{
		long nextResetTime = TimeUtil.getNextTime(6, 30).getTimeInMillis();
		long currentTime = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.set(11, 6);
		calendar.set(12, 30);
		calendar.set(13, 0);
		calendar.set(14, 0);
		long currentResetTime = calendar.getTimeInMillis();
		if (currentTime >= currentResetTime && GlobalVariablesManager.getInstance().getLong("DAILY_TASK_RESET", 0L) <= currentResetTime)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Daily task will run now.");
			this.onReset();
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Next schedule at " + TimeUtil.getDateTimeString(nextResetTime) + ".");
		}

		long startDelay = Math.max(0L, nextResetTime - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onReset, startDelay, 86400000L);
		ThreadPool.scheduleAtFixedRate(this::onSave, 1800000L, 1800000L);
	}

	private void onReset()
	{
		LOGGER.info("Starting reset of daily tasks...");
		GlobalVariablesManager.getInstance().set("DAILY_TASK_RESET", System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(7) == 4)
		{
			clanLeaderApply();
			resetMonsterArenaWeekly();
			resetTimedHuntingZonesWeekly();
			resetVitalityWeekly();
			resetPrivateStoreHistory();
			resetWeeklyLimitShopData();
		}
		else
		{
			resetVitalityDaily();
		}

		if (HuntPassConfig.ENABLE_HUNT_PASS && calendar.get(5) == HuntPassConfig.HUNT_PASS_PERIOD)
		{
			this.resetHuntPass();
		}

		if (calendar.get(5) == 1)
		{
			this.resetMonthlyLimitShopData();
		}

		resetClanBonus();
		resetClanContributionList();
		resetClanDonationPoints();
		resetDailyHennaPattern();
		resetDailyPouchExtract();
		resetDailySkills();
		resetDailyItems();
		resetDailyPrimeShopData();
		resetDailyLimitShopData();
		resetWorldChatPoints();
		resetRecommends();
		resetTrainingCamp();
		resetTimedHuntingZones();
		resetMorgosMilitaryBase();
		resetDailyMissionRewards();
		resetAttendanceRewards();
		resetVip();
		resetResurrectionByPayment();
		checkWeekSwap();
		if (EventDispatcher.getInstance().hasListener(EventType.ON_DAILY_RESET))
		{
			EventDispatcher.getInstance().notifyEvent(new OnDailyReset());
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().storeMe();
			player.getAccountVariables().storeMe();
		}

		LOGGER.info("Daily tasks reset completed.");
	}

	private static void checkWeekSwap()
	{
		long nextEvenWeekSwap = GlobalVariablesManager.getInstance().getLong("NEXT_EVEN_WEEK_SWAP", 0L);
		if (nextEvenWeekSwap < System.currentTimeMillis())
		{
			boolean isEvenWeek = GlobalVariablesManager.getInstance().getBoolean("IS_EVEN_WEEK", true);
			GlobalVariablesManager.getInstance().set("IS_EVEN_WEEK", !isEvenWeek);
			Calendar calendar = TimeUtil.getNextDayTime(4, 6, 25);
			GlobalVariablesManager.getInstance().set("NEXT_EVEN_WEEK_SWAP", calendar.getTimeInMillis());
		}
	}

	private void onSave()
	{
		GlobalVariablesManager.getInstance().storeMe();
		BlackCouponManager.getInstance().storeMe();
		RevengeHistoryManager.getInstance().storeMe();
		if (WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
		{
			WorldExchangeManager.getInstance().storeMe();
		}

		if (Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			LOGGER.info("Olympiad System: Data updated.");
		}

		MableGameData.getInstance().save();
	}

	private static void clanLeaderApply()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getNewLeaderId() != 0)
			{
				ClanMember member = clan.getClanMember(clan.getNewLeaderId());
				if (member != null)
				{
					clan.setNewLeader(member);
				}
			}
		}

		LOGGER.info("Clan leaders have been updated.");
	}

	private static void resetClanContributionList()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			clan.getVariables().deleteWeeklyContribution();
		}
	}

	private static void resetVitalityDaily()
	{
		if (PlayerConfig.ENABLE_VITALITY)
		{
			int vitality = 875000;

			for (Player player : World.getInstance().getPlayers())
			{
				int VP = player.getVitalityPoints();
				player.setVitalityPoints(VP + vitality, false);

				for (SubClassHolder subclass : player.getSubClasses().values())
				{
					int VPS = subclass.getVitalityPoints();
					subclass.setVitalityPoints(VPS + vitality);
				}
			}

			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement st = con.prepareStatement("UPDATE character_subclasses SET vitality_points = IF(vitality_points = ?, vitality_points, vitality_points + ?)"))
				{
					st.setInt(1, 3500000);
					st.setInt(2, 875000);
					st.execute();
				}

				try (PreparedStatement st = con.prepareStatement("UPDATE characters SET vitality_points = IF(vitality_points = ?, vitality_points, vitality_points + ?)"))
				{
					st.setInt(1, 3500000);
					st.setInt(2, 875000);
					st.execute();
				}
			}
			catch (Exception var14)
			{
				LOGGER.log(Level.WARNING, "Error while updating vitality", var14);
			}

			LOGGER.info("Daily vitality added successfully.");
		}
	}

	private static void resetVitalityWeekly()
	{
		if (PlayerConfig.ENABLE_VITALITY)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				player.setVitalityPoints(3500000, false);

				for (SubClassHolder subclass : player.getSubClasses().values())
				{
					subclass.setVitalityPoints(3500000);
				}
			}

			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement st = con.prepareStatement("UPDATE character_subclasses SET vitality_points = ?"))
				{
					st.setInt(1, 3500000);
					st.execute();
				}

				try (PreparedStatement st = con.prepareStatement("UPDATE characters SET vitality_points = ?"))
				{
					st.setInt(1, 3500000);
					st.execute();
				}
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Error while updating vitality", var11);
			}

			LOGGER.info("Vitality points have been reset.");
		}
	}

	private static void resetMonsterArenaWeekly()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			GlobalVariablesManager.getInstance().remove("MA_C" + clan.getId());
		}
	}

	private static void resetClanBonus()
	{
		ClanTable.getInstance().getClans().forEach(Clan::resetClanBonus);
		LOGGER.info("Daily clan bonuses have been reset.");
	}

	private static void resetDailySkills()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (int skillId : RESET_SKILLS)
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE skill_id=? AND charId IN (SELECT charId FROM characters WHERE online = 0)"))
				{
					ps.setInt(1, skillId);
					ps.execute();
				}
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.SEVERE, "Could not reset daily skill reuse: ", var11);
		}

		for (int skillId : RESET_SKILLS)
		{
			Skill skill = SkillData.getInstance().getSkill(skillId, 1);
			if (skill != null)
			{
				for (Player player : World.getInstance().getPlayers())
				{
					if (player.hasSkillReuse(skill.getReuseHashCode()))
					{
						player.removeTimeStamp(skill);
					}
				}
			}
		}

		LOGGER.info("Daily skill reuse cleaned.");
	}

	private static void resetDailyItems()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (int itemId : RESET_ITEMS)
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_item_reuse_save WHERE itemId=? AND charId IN (SELECT charId FROM characters WHERE online = 0)"))
				{
					ps.setInt(1, itemId);
					ps.execute();
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not reset daily item reuse: ", var12);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			boolean update = false;

			for (int itemId : RESET_ITEMS)
			{
				for (Item item : player.getInventory().getAllItemsByItemId(itemId))
				{
					player.getItemReuseTimeStamps().remove(item.getObjectId());
					update = true;
				}
			}

			if (update)
			{
				player.sendItemList();
			}
		}

		LOGGER.info("Daily item reuse cleaned.");
	}

	private static void resetClanDonationPoints()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
		{
			ps.setString(1, "CLAN_DONATION_POINTS");
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not reset clan donation points: ", var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove("CLAN_DONATION_POINTS");
		}

		LOGGER.info("Daily clan donation points have been reset.");
	}

	private static void resetWorldChatPoints()
	{
		if (GeneralConfig.ENABLE_WORLD_CHAT)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE character_variables SET val = ? WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
			{
				ps.setInt(1, 0);
				ps.setString(2, "WORLD_CHAT_USED");
				ps.executeUpdate();
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.SEVERE, "Could not reset daily world chat points: ", var9);
			}

			for (Player player : World.getInstance().getPlayers())
			{
				player.setWorldChatUsed(0);
				player.sendPacket(new ExWorldChatCnt(player));
			}

			LOGGER.info("Daily world chat points have been reset.");
		}
	}

	private static void resetRecommends()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left = ?, rec_have = 0 WHERE rec_have <= 20"))
			{
				ps.setInt(1, 0);
				ps.execute();
			}

			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left = ?, rec_have = GREATEST(rec_have - 20,0) WHERE rec_have > 20"))
			{
				ps.setInt(1, 0);
				ps.execute();
			}
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.SEVERE, "Could not reset Recommendations System: ", var11);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.setRecomLeft(0);
			player.setRecomHave(player.getRecomHave() - 20);
			player.sendPacket(new ExVoteSystemInfo(player));
			player.broadcastUserInfo();
		}
	}

	private static void resetTrainingCamp()
	{
		if (TrainingCampConfig.TRAINING_CAMP_ENABLE)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ? AND account_name NOT IN (SELECT account_name FROM characters WHERE online = 1)");)
			{
				ps.setString(1, "TRAINING_CAMP_DURATION");
				ps.executeUpdate();
			}
			catch (Exception var9)
			{
				LOGGER.log(Level.SEVERE, "Could not reset Training Camp: ", var9);
			}

			for (Player player : World.getInstance().getPlayers())
			{
				player.resetTraingCampDuration();
			}

			LOGGER.info("Training Camp durations have been reset.");
		}
	}

	private static void resetVip()
	{
		AccountVariables.deleteVipPurchases("Vip_Item_Bought");

		for (Player player : World.getInstance().getPlayers())
		{
			if (player.getVipTier() > 0)
			{
				VipManager.getInstance().checkVipTierExpiration(player);
			}

			player.getAccountVariables().restoreMe();
		}
	}

	private static void resetDailyMissionRewards()
	{
		DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::reset);
	}

	private static void resetTimedHuntingZones()
	{
		for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
		{
			if (!holder.isWeekly())
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var IN (?, ?, ?) AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
				{
					ps.setString(1, "HUNTING_ZONE_ENTRY_" + holder.getZoneId());
					ps.setString(2, "HUNTING_ZONE_TIME_" + holder.getZoneId());
					ps.setString(3, "HUNTING_ZONE_REMAIN_REFILL_" + holder.getZoneId());
					ps.executeUpdate();
				}
				catch (Exception var11)
				{
					LOGGER.log(Level.SEVERE, "Could not reset Special Hunting Zones: ", var11);
				}

				for (Player player : World.getInstance().getPlayers())
				{
					player.getVariables().remove("HUNTING_ZONE_ENTRY_" + holder.getZoneId());
					player.getVariables().remove("HUNTING_ZONE_TIME_" + holder.getZoneId());
					player.getVariables().remove("HUNTING_ZONE_REMAIN_REFILL_" + holder.getZoneId());
				}
			}
		}

		LOGGER.info("Special Hunting Zones have been reset.");
	}

	private static void resetTimedHuntingZonesWeekly()
	{
		for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
		{
			if (holder.isWeekly())
			{
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var IN (?, ?, ?) AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
				{
					ps.setString(1, "HUNTING_ZONE_ENTRY_" + holder.getZoneId());
					ps.setString(2, "HUNTING_ZONE_TIME_" + holder.getZoneId());
					ps.setString(3, "HUNTING_ZONE_REMAIN_REFILL_" + holder.getZoneId());
					ps.executeUpdate();
				}
				catch (Exception var11)
				{
					LOGGER.log(Level.SEVERE, "Could not reset Weekly Special Hunting Zones: ", var11);
				}

				for (Player player : World.getInstance().getPlayers())
				{
					player.getVariables().remove("HUNTING_ZONE_ENTRY_" + holder.getZoneId());
					player.getVariables().remove("HUNTING_ZONE_TIME_" + holder.getZoneId());
					player.getVariables().remove("HUNTING_ZONE_REMAIN_REFILL_" + holder.getZoneId());
				}
			}
		}

		LOGGER.info("Weekly Special Hunting Zones have been reset.");
	}

	private void resetAttendanceRewards()
	{
		if (AttendanceRewardsConfig.ATTENDANCE_REWARDS_SHARE_ACCOUNT)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ? AND account_name NOT IN (SELECT account_name FROM characters WHERE online = 1)");)
			{
				ps.setString(1, "ATTENDANCE_DATE");
				ps.execute();
			}
			catch (Exception var14)
			{
				LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset Attendance Rewards: " + var14);
			}

			for (Player player : World.getInstance().getPlayers())
			{
				player.getAccountVariables().remove("ATTENDANCE_DATE");
			}

			LOGGER.info("Account shared Attendance Rewards have been reset.");
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
			{
				ps.setString(1, "ATTENDANCE_DATE");
				ps.execute();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset Attendance Rewards: " + var11);
			}

			for (Player player : World.getInstance().getPlayers())
			{
				player.getVariables().remove("ATTENDANCE_DATE");
			}

			LOGGER.info("Attendance Rewards have been reset.");
		}
	}

	private void resetDailyPrimeShopData()
	{
		for (PrimeShopGroup holder : PrimeShopData.getInstance().getPrimeItems().values())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ? AND account_name NOT IN (SELECT account_name FROM characters WHERE online = 1)");)
			{
				ps.setString(1, "PSPDailyCount" + holder.getBrId());
				ps.executeUpdate();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset PrimeShopData: " + var11);
			}

			for (Player player : World.getInstance().getPlayers())
			{
				player.getAccountVariables().remove("PSPDailyCount" + holder.getBrId());
			}
		}

		LOGGER.info("PrimeShopData have been reset.");
	}

	private void resetDailyLimitShopData()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ? AND account_name NOT IN (SELECT account_name FROM characters WHERE online = 1)");)
		{
			for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
			{
				ps.setString(1, "LCSDailyCount" + holder.getProductionId());
				ps.executeUpdate();
			}

			for (LimitShopProductHolder holder : LimitShopCraftData.getInstance().getProducts())
			{
				ps.setString(1, "LCSDailyCount" + holder.getProductionId());
				ps.executeUpdate();
			}
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset LimitShopData: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
			{
				player.getAccountVariables().remove("LCSDailyCount" + holder.getProductionId());
			}

			for (LimitShopProductHolder holder : LimitShopCraftData.getInstance().getProducts())
			{
				player.getAccountVariables().remove("LCSDailyCount" + holder.getProductionId());
			}
		}

		LOGGER.info("Daily LimitShopData have been reset.");
	}

	private void resetWeeklyLimitShopData()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ? AND account_name NOT IN (SELECT account_name FROM characters WHERE online = 1)");)
		{
			for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
			{
				ps.setString(1, "LCSWeeklyCount" + holder.getProductionId());
				ps.executeUpdate();
			}

			for (LimitShopProductHolder holder : LimitShopCraftData.getInstance().getProducts())
			{
				ps.setString(1, "LCSWeeklyCount" + holder.getProductionId());
				ps.executeUpdate();
			}
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset LimitShopData: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
			{
				player.getAccountVariables().remove("LCSWeeklyCount" + holder.getProductionId());
			}

			for (LimitShopProductHolder holder : LimitShopCraftData.getInstance().getProducts())
			{
				player.getAccountVariables().remove("LCSWeeklyCount" + holder.getProductionId());
			}
		}

		LOGGER.info("Weekly LimitShopData have been reset.");
	}

	private void resetMonthlyLimitShopData()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ? AND account_name NOT IN (SELECT account_name FROM characters WHERE online = 1)");)
		{
			for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
			{
				ps.setString(1, "LCSMonthlyCount" + holder.getProductionId());
				ps.executeUpdate();
			}

			for (LimitShopProductHolder holder : LimitShopCraftData.getInstance().getProducts())
			{
				ps.setString(1, "LCSMonthlyCount" + holder.getProductionId());
				ps.executeUpdate();
			}
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset LimitShopData: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
			{
				player.getAccountVariables().remove("LCSMonthlyCount" + holder.getProductionId());
			}

			for (LimitShopProductHolder holder : LimitShopCraftData.getInstance().getProducts())
			{
				player.getAccountVariables().remove("LCSMonthlyCount" + holder.getProductionId());
			}
		}

		LOGGER.info("Monthly LimitShopData have been reset.");
	}

	private void resetHuntPass()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM huntpass");)
		{
			statement.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not delete entries from hunt pass: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getHuntPass().resetHuntPass();
		}

		LOGGER.info("HuntPassData have been reset.");
	}

	private void resetResurrectionByPayment()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
		{
			ps.setString(1, "RESURRECT_BY_PAYMENT_COUNT");
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset payment resurrection count for players: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove("RESURRECT_BY_PAYMENT_COUNT");
		}

		LOGGER.info("Daily payment resurrection count for player have been reset.");
	}

	public void resetPrivateStoreHistory()
	{
		try
		{
			PrivateStoreHistoryManager.getInstance().reset();
		}
		catch (Exception var2)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset private store history! " + var2);
		}

		LOGGER.info("Private store history records have been reset.");
	}

	private void resetDailyHennaPattern()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
		{
			ps.setString(1, "DYE_POTENTIAL_DAILY_COUNT");
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset Daily Henna Count: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove("DYE_POTENTIAL_DAILY_COUNT");
		}

		LOGGER.info("Daily Henna Count have been reset.");
	}

	private void resetMorgosMilitaryBase()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
		{
			ps.setString(1, "MORGOS_MILITARY_FREE");
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset MorgosMilitaryBase: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getAccountVariables().remove("MORGOS_MILITARY_FREE");
		}

		LOGGER.info("MorgosMilitaryBase have been reset.");
	}

	private void resetDailyPouchExtract()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ? AND charId IN (SELECT charId FROM characters WHERE online = 0)");)
		{
			ps.setString(1, "DAILY_EXTRACT_ITEM98232");
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Could not reset Daily Pouch Extract: " + var9);
		}

		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove("DAILY_EXTRACT_ITEM98232");
		}

		LOGGER.info("Daily Pouch Extract Count have been reset.");
	}

	public static DailyResetManager getInstance()
	{
		return DailyResetManager.SingletonHolder.INSTANCE;
	}

	static
	{
		RESET_SKILLS.add(39199);
		RESET_ITEMS.add(49782);
	}

	private static class SingletonHolder
	{
		protected static final DailyResetManager INSTANCE = new DailyResetManager();
	}
}
