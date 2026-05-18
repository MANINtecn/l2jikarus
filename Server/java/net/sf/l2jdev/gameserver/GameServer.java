package net.sf.l2jdev.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.config.InterfaceConfig;
import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.ConnectionManager;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.DeadlockWatcher;
import net.sf.l2jdev.gameserver.cache.HtmCache;
import net.sf.l2jdev.gameserver.config.AdenLaboratoryConfig;
import net.sf.l2jdev.gameserver.config.ConfigLoader;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.custom.CustomMailManagerConfig;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.SellBuffsConfig;
import net.sf.l2jdev.gameserver.config.custom.WeddingConfig;
import net.sf.l2jdev.gameserver.data.BotReportTable;
import net.sf.l2jdev.gameserver.data.SchemeBufferTable;
import net.sf.l2jdev.gameserver.data.sql.AnnouncementsTable;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.sql.CharSummonTable;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.data.sql.OfflinePlayTable;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.data.sql.PartyMatchingHistoryTable;
import net.sf.l2jdev.gameserver.data.xml.ActionData;
import net.sf.l2jdev.gameserver.data.xml.AdenLaboratoryData;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.data.xml.AgathionData;
import net.sf.l2jdev.gameserver.data.xml.AppearanceItemData;
import net.sf.l2jdev.gameserver.data.xml.ArmorSetData;
import net.sf.l2jdev.gameserver.data.xml.AttendanceRewardData;
import net.sf.l2jdev.gameserver.data.xml.BeautyShopData;
import net.sf.l2jdev.gameserver.data.xml.BuyListData;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.data.xml.CharacterStylesData;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.data.xml.ClanLevelData;
import net.sf.l2jdev.gameserver.data.xml.ClanRewardData;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.data.xml.CombinationItemsData;
import net.sf.l2jdev.gameserver.data.xml.CubicData;
import net.sf.l2jdev.gameserver.data.xml.DailyMissionData;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.DynamicExpRateData;
import net.sf.l2jdev.gameserver.data.xml.ElementalAttributeData;
import net.sf.l2jdev.gameserver.data.xml.ElementalSpiritData;
import net.sf.l2jdev.gameserver.data.xml.EnchantChallengePointData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemGroupsData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemHPBonusData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemOptionsData;
import net.sf.l2jdev.gameserver.data.xml.EnsoulData;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeData;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeNormalData;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.ExperienceLossData;
import net.sf.l2jdev.gameserver.data.xml.FenceData;
import net.sf.l2jdev.gameserver.data.xml.FishingData;
import net.sf.l2jdev.gameserver.data.xml.HennaCombinationData;
import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;
import net.sf.l2jdev.gameserver.data.xml.HitConditionBonusData;
import net.sf.l2jdev.gameserver.data.xml.InitialEquipmentData;
import net.sf.l2jdev.gameserver.data.xml.InitialShortcutData;
import net.sf.l2jdev.gameserver.data.xml.ItemCrystallizationData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.KarmaLossData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopClanData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopCraftData;
import net.sf.l2jdev.gameserver.data.xml.LimitShopData;
import net.sf.l2jdev.gameserver.data.xml.LuckyGameData;
import net.sf.l2jdev.gameserver.data.xml.MableGameData;
import net.sf.l2jdev.gameserver.data.xml.MagicLampData;
import net.sf.l2jdev.gameserver.data.xml.MissionLevel;
import net.sf.l2jdev.gameserver.data.xml.MultisellData;
import net.sf.l2jdev.gameserver.data.xml.NewQuestData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.NpcNameLocalisationData;
import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.data.xml.PetAcquireList;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.PetExtractData;
import net.sf.l2jdev.gameserver.data.xml.PetSkillData;
import net.sf.l2jdev.gameserver.data.xml.PetTypeData;
import net.sf.l2jdev.gameserver.data.xml.PlayerTemplateData;
import net.sf.l2jdev.gameserver.data.xml.PrimeShopData;
import net.sf.l2jdev.gameserver.data.xml.RaidDropAnnounceData;
import net.sf.l2jdev.gameserver.data.xml.RaidTeleportListData;
import net.sf.l2jdev.gameserver.data.xml.RandomCraftData;
import net.sf.l2jdev.gameserver.data.xml.RecipeData;
import net.sf.l2jdev.gameserver.data.xml.RelicCollectionData;
import net.sf.l2jdev.gameserver.data.xml.RelicCouponData;
import net.sf.l2jdev.gameserver.data.xml.ResidenceFunctionsData;
import net.sf.l2jdev.gameserver.data.xml.SayuneData;
import net.sf.l2jdev.gameserver.data.xml.SecondaryAuthData;
import net.sf.l2jdev.gameserver.data.xml.SendMessageLocalisationData;
import net.sf.l2jdev.gameserver.data.xml.ShuttleData;
import net.sf.l2jdev.gameserver.data.xml.SiegeScheduleData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillEnchantData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.data.xml.SpawnData;
import net.sf.l2jdev.gameserver.data.xml.StaticObjectData;
import net.sf.l2jdev.gameserver.data.xml.SubjugationData;
import net.sf.l2jdev.gameserver.data.xml.SubjugationGacha;
import net.sf.l2jdev.gameserver.data.xml.TeleportListData;
import net.sf.l2jdev.gameserver.data.xml.TeleporterData;
import net.sf.l2jdev.gameserver.data.xml.TimedHuntingZoneData;
import net.sf.l2jdev.gameserver.data.xml.TransformData;
import net.sf.l2jdev.gameserver.data.xml.VariationData;
import net.sf.l2jdev.gameserver.data.xml.VipData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.handler.ConditionHandler;
import net.sf.l2jdev.gameserver.handler.DailyMissionHandler;
import net.sf.l2jdev.gameserver.handler.EffectHandler;
import net.sf.l2jdev.gameserver.handler.SkillConditionHandler;
import net.sf.l2jdev.gameserver.managers.AirShipManager;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.managers.BoatManager;
import net.sf.l2jdev.gameserver.managers.CaptchaManager;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.managers.ClanHallAuctionManager;
import net.sf.l2jdev.gameserver.managers.CoupleManager;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.CustomMailManager;
import net.sf.l2jdev.gameserver.managers.DailyResetManager;
import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.managers.FakePlayerChatManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.managers.GrandBossManager;
import net.sf.l2jdev.gameserver.managers.IdManager;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.ItemAuctionManager;
import net.sf.l2jdev.gameserver.managers.ItemCommissionManager;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.managers.PcCafePointsManager;
import net.sf.l2jdev.gameserver.managers.PetitionManager;
import net.sf.l2jdev.gameserver.managers.PrecautionaryRestartManager;
import net.sf.l2jdev.gameserver.managers.PremiumManager;
import net.sf.l2jdev.gameserver.managers.PrivateStoreHistoryManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.managers.PurgeRankingManager;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.managers.RankingPowerManager;
import net.sf.l2jdev.gameserver.managers.RevengeHistoryManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.managers.SellBuffsManager;
import net.sf.l2jdev.gameserver.managers.ServerRestartManager;
import net.sf.l2jdev.gameserver.model.voicechat.VoiceChatManager;
import net.sf.l2jdev.gameserver.managers.SharedTeleportManager;
import net.sf.l2jdev.gameserver.managers.SiegeGuardManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.managers.TreasureManager;
import net.sf.l2jdev.gameserver.managers.WalkingManager;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.managers.events.EventDropManager;
import net.sf.l2jdev.gameserver.managers.events.LeonasDungeonManager;
import net.sf.l2jdev.gameserver.managers.games.MonsterRaceManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.OnServerStart;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.vip.VipManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.GamePacketHandler;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.scripting.ScriptEngine;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.ItemLifeTimeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.ItemsAutoDestroyTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class GameServer
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	private static final long START_TIME = System.currentTimeMillis();

	public GameServer() throws Exception
	{
		InterfaceConfig.load();
		if (InterfaceConfig.ENABLE_GUI)
		{
			System.out.println("GameServer: Running in GUI mode.");
			 new GameServerLaucher();
		}

		File logFolder = new File(".", "log");
		logFolder.mkdir();
		InputStream is = new FileInputStream(new File("./log.cfg"));

		try
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		catch (Throwable var7)
		{
			try
			{
				is.close();
			}
			catch (Throwable var5)
			{
				var7.addSuppressed(var5);
			}

			throw var7;
		}

		is.close();
		ConfigLoader.init();
		GameServer.printSection("Database");
		DatabaseFactory.init();
		GameServer.printSection("ThreadPool");
		ThreadPool.init();
		GameTimeTaskManager.getInstance();
		GameServer.printSection("IdManager");
		IdManager.getInstance();
		GameServer.printSection("Scripting Engine");
		EventDispatcher.getInstance();
		ScriptEngine.getInstance();
		GameServer.printSection("World");
		World.getInstance();
		MapRegionManager.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		FenceData.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		GameServer.printSection("Data");
		ActionData.getInstance();
		CategoryData.getInstance();
		DynamicExpRateData.getInstance();
		SecondaryAuthData.getInstance();
		SayuneData.getInstance();
		ClanRewardData.getInstance();
		MissionLevel.getInstance();
		DailyMissionHandler.getInstance().executeScript();
		DailyMissionData.getInstance();
		ElementalSpiritData.getInstance();
		RankingPowerManager.getInstance();
		SubjugationData.getInstance();
		SubjugationGacha.getInstance();
		PurgeRankingManager.getInstance();
		if (AdenLaboratoryConfig.ADENLAB_ENABLED)
		{
			GameServer.printSection("Aden Laboratory");
			AdenLaboratoryData.getInstance().load();
		}

		GameServer.printSection("Skills");
		SkillConditionHandler.getInstance().executeScript();
		EffectHandler.getInstance().executeScript();
		TransformData.getInstance();
		SkillData.getInstance();
		SkillTreeData.getInstance();
		PetSkillData.getInstance();
		PetAcquireList.getInstance();
		SkillEnchantData.getInstance();
		GameServer.printSection("Items");
		ConditionHandler.getInstance().executeScript();
		ItemData.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		EnchantChallengePointData.getInstance();
		ElementalAttributeData.getInstance();
		ItemCrystallizationData.getInstance();
		OptionData.getInstance();
		VariationData.getInstance();
		EnsoulData.getInstance();
		EnchantItemHPBonusData.getInstance();
		BuyListData.getInstance();
		MultisellData.getInstance();
		CombinationItemsData.getInstance();
		EquipmentUpgradeData.getInstance();
		EquipmentUpgradeNormalData.getInstance();
		AgathionData.getInstance();
		RaidTeleportListData.getInstance();
		RecipeData.getInstance();
		ArmorSetData.getInstance();
		FishingData.getInstance();
		HennaData.getInstance();
		HennaCombinationData.getInstance();
		HennaPatternPotentialData.getInstance();
		PrimeShopData.getInstance();
		LimitShopData.getInstance();
		LimitShopCraftData.getInstance();
		LimitShopClanData.getInstance();
		CollectionData.getInstance();
		RelicCollectionData.getInstance();
		RelicCouponData.getInstance();
		RaidDropAnnounceData.getInstance();
		PcCafePointsManager.getInstance();
		AppearanceItemData.getInstance();
		BlackCouponManager.getInstance();
		ItemCommissionManager.getInstance();
		WorldExchangeManager.getInstance();
		PrivateStoreHistoryManager.getInstance().restore();
		LuckyGameData.getInstance();
		MableGameData.getInstance();
		AttendanceRewardData.getInstance();
		MagicLampData.getInstance();
		RandomCraftData.getInstance();
		RevengeHistoryManager.getInstance();
		VipData.getInstance();
		ItemLifeTimeTaskManager.getInstance();
		GameServer.printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		ExperienceLossData.getInstance();
		KarmaLossData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharInfoTable.getInstance();
		PartyMatchingHistoryTable.getInstance();
		AdminData.getInstance();
		PetDataTable.getInstance();
		PetTypeData.getInstance();
		PetExtractData.getInstance();
		CubicData.getInstance();
		CharSummonTable.getInstance().init();
		CaptchaManager.getInstance();
		BeautyShopData.getInstance();
		MentorManager.getInstance();
		VipManager.getInstance();
		CharacterStylesData.getInstance();
		if (PremiumSystemConfig.PREMIUM_SYSTEM_ENABLED)
		{
			LOGGER.info("PremiumManager: Premium system is enabled.");
			PremiumManager.getInstance();
		}

		GameServer.printSection("Clans");
		ClanLevelData.getInstance();
		ClanTable.getInstance();
		ResidenceFunctionsData.getInstance();
		ClanHallData.getInstance();
		ClanHallAuctionManager.getInstance();
		ClanEntryManager.getInstance();
		GameServer.printSection("Geodata");
		GeoEngine.getInstance();
		GameServer.printSection("NPCs");
		NpcData.getInstance();
		FakePlayerChatManager.getInstance();
		SpawnData.getInstance();
		WalkingManager.getInstance();
		StaticObjectData.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		SchemeBufferTable.getInstance();
		GrandBossManager.getInstance();
		EventDropManager.getInstance();
		LeonasDungeonManager.getInstance();
		GameServer.printSection("Instance");
		InstanceManager.getInstance();
		GameServer.printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		GameServer.printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportListData.getInstance();
		SharedTeleportManager.getInstance();
		TeleporterData.getInstance();
		TimedHuntingZoneData.getInstance();
		MatchingRoomManager.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		BotReportTable.getInstance();
		RankManager.getInstance();
		if (SellBuffsConfig.SELLBUFF_ENABLED)
		{
			SellBuffsManager.getInstance();
		}

		if (MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			SystemMessageId.loadLocalisations();
			NpcStringId.loadLocalisations();
			SendMessageLocalisationData.getInstance();
			NpcNameLocalisationData.getInstance();
		}

		GameServer.printSection("Scripts");
		NewQuestData.getInstance();
		ScriptManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		ShuttleData.getInstance();

		try
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loading server scripts:");
			ScriptEngine.getInstance().executeScript(ScriptEngine.MASTER_HANDLER_FILE);
			ScriptEngine.getInstance().executeScriptList();
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to execute script list!", var6);
		}

		SpawnData.getInstance().init();
		DatabaseSpawnManager.getInstance();
		GameServer.printSection("Siege");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().loadInstances();
		FortManager.getInstance().activateInstances();
		FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();
		CastleManorManager.getInstance();
		SiegeGuardManager.getInstance();
		ScriptManager.getInstance().report();
		if (GeneralConfig.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}

		if (GeneralConfig.AUTODESTROY_ITEM_AFTER > 0 || GeneralConfig.HERB_AUTO_DESTROY_TIME > 0)
		{
			ItemsAutoDestroyTaskManager.getInstance();
		}

		MonsterRaceManager.getInstance();
		if (WeddingConfig.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}

		DailyResetManager.getInstance();
		TreasureManager.getInstance();
		AntiFeedManager.getInstance().registerEvent(0);
		if (OfflinePlayConfig.ENABLE_OFFLINE_PLAY_COMMAND)
		{
			AntiFeedManager.getInstance().registerEvent(4);
		}

		if (GeneralConfig.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}

		if (CustomMailManagerConfig.CUSTOM_MAIL_MANAGER_ENABLED)
		{
			CustomMailManager.getInstance();
		}

		if (EventDispatcher.getInstance().hasListener(EventType.ON_SERVER_START))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnServerStart());
		}

		PunishmentManager.getInstance();
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		LOGGER.info("IdManager: Free ObjectID's remaining: " + IdManager.getInstance().getAvailableIdCount());
		if ((OfflineTradeConfig.OFFLINE_TRADE_ENABLE || OfflineTradeConfig.OFFLINE_CRAFT_ENABLE) && OfflineTradeConfig.RESTORE_OFFLINERS)
		{
			OfflineTraderTable.getInstance().restoreOfflineTraders();
		}

		if (OfflinePlayConfig.ENABLE_OFFLINE_PLAY_COMMAND && OfflinePlayConfig.RESTORE_AUTO_PLAY_OFFLINERS)
		{
			OfflinePlayTable.getInstance().restoreOfflinePlayers();
		}

		if (ServerConfig.SERVER_RESTART_SCHEDULE_ENABLED)
		{
			ServerRestartManager.getInstance();
		}

		if (ServerConfig.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance();
		}

		if (ServerConfig.DEADLOCK_WATCHER)
		{
			DeadlockWatcher deadlockWatcher = new DeadlockWatcher(Duration.ofSeconds(ServerConfig.DEADLOCK_CHECK_INTERVAL), () -> {
				if (ServerConfig.RESTART_ON_DEADLOCK)
				{
					Broadcast.toAllOnlinePlayers("Server has stability issues - restarting now.");
					Shutdown.getInstance().startShutdown(null, 60, true);
				}
			});
			deadlockWatcher.setDaemon(true);
			deadlockWatcher.start();
		}

		System.gc();
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576L;
		LOGGER.info(this.getClass().getSimpleName() + ": Started, using " + this.getUsedMemoryMB() + " of " + totalMem + " MB total memory.");
		LOGGER.info(this.getClass().getSimpleName() + ": Maximum number of connected players is " + ServerConfig.MAXIMUM_ONLINE_USERS + ".");
		LOGGER.info(this.getClass().getSimpleName() + ": Server loaded in " + (System.currentTimeMillis() - START_TIME) / 1000L + " seconds.");
		new ConnectionManager<>(new InetSocketAddress(ServerConfig.PORT_GAME), GameClient::new, new GamePacketHandler());
		VoiceChatManager.getInstance().start();
		LoginServerThread.getInstance().start();
	}

	private static void printSection(String section)
	{
		String s = "=[ " + section + " ]";

		while (s.length() < 61)
		{
			s = "-" + s;
		}

		LOGGER.info(s);
	}

	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
	}

	public static long getStartTime()
	{
		return START_TIME;
	}

	public static void main(String[] args) throws Exception
	{
		new GameServer();
	}
}
