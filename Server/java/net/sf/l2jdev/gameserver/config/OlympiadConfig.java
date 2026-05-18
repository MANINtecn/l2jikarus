package net.sf.l2jdev.gameserver.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.ConfigReader;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class OlympiadConfig
{
	private static final Logger LOGGER = Logger.getLogger(OlympiadConfig.class.getName());
	public static final String OLYMPIAD_CONFIG_FILE = "./config/Olympiad.ini";
	public static boolean OLYMPIAD_ENABLED;
	public static int OLYMPIAD_START_TIME;
	public static int OLYMPIAD_MIN;
	public static long OLYMPIAD_CPERIOD;
	public static long OLYMPIAD_BATTLE;
	public static long OLYMPIAD_WPERIOD;
	public static long OLYMPIAD_VPERIOD;
	public static int OLYMPIAD_START_POINTS;
	public static int OLYMPIAD_WEEKLY_POINTS;
	public static int OLYMPIAD_CLASSED;
	public static int OLYMPIAD_NONCLASSED;
	public static List<ItemHolder> OLYMPIAD_WINNER_REWARD;
	public static List<ItemHolder> OLYMPIAD_LOSER_REWARD;
	public static int OLYMPIAD_COMP_RITEM;
	public static int OLYMPIAD_MIN_MATCHES;
	public static int OLYMPIAD_MARK_PER_POINT;
	public static int OLYMPIAD_HERO_POINTS;
	public static int OLYMPIAD_RANK1_POINTS;
	public static int OLYMPIAD_RANK2_POINTS;
	public static int OLYMPIAD_RANK3_POINTS;
	public static int OLYMPIAD_RANK4_POINTS;
	public static int OLYMPIAD_RANK5_POINTS;
	public static int OLYMPIAD_MAX_POINTS;
	public static int OLYMPIAD_DIVIDER_CLASSED;
	public static int OLYMPIAD_DIVIDER_NON_CLASSED;
	public static int OLYMPIAD_MAX_WEEKLY_MATCHES;
	public static boolean OLYMPIAD_LOG_FIGHTS;
	public static boolean OLYMPIAD_SHOW_MONTHLY_WINNERS;
	public static boolean OLYMPIAD_ANNOUNCE_GAMES;
	public static Set<Integer> LIST_OLY_RESTRICTED_ITEMS = new HashSet<>();
	public static int OLYMPIAD_WEAPON_ENCHANT_LIMIT;
	public static int OLYMPIAD_ARMOR_ENCHANT_LIMIT;
	public static int OLYMPIAD_WAIT_TIME;
	public static String OLYMPIAD_PERIOD;
	public static int OLYMPIAD_PERIOD_MULTIPLIER;
	public static List<Integer> OLYMPIAD_COMPETITION_DAYS;
	public static boolean OLYMPIAD_HIDE_NAMES;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Olympiad.ini");
		OLYMPIAD_ENABLED = config.getBoolean("OlympiadEnabled", true);
		OLYMPIAD_START_TIME = config.getInt("OlympiadStartTime", 20);
		OLYMPIAD_MIN = config.getInt("OlympiadMin", 0);
		OLYMPIAD_CPERIOD = config.getLong("OlympiadCPeriod", 14400000L);
		OLYMPIAD_BATTLE = config.getLong("OlympiadBattle", 300000L);
		OLYMPIAD_WPERIOD = config.getLong("OlympiadWPeriod", 604800000L);
		OLYMPIAD_VPERIOD = config.getLong("OlympiadVPeriod", 86400000L);
		OLYMPIAD_START_POINTS = config.getInt("OlympiadStartPoints", 10);
		OLYMPIAD_WEEKLY_POINTS = config.getInt("OlympiadWeeklyPoints", 10);
		OLYMPIAD_CLASSED = config.getInt("OlympiadClassedParticipants", 10);
		OLYMPIAD_NONCLASSED = config.getInt("OlympiadNonClassedParticipants", 20);
		OLYMPIAD_WINNER_REWARD = parseItemsList(config.getString("OlympiadWinReward", "none"));
		OLYMPIAD_LOSER_REWARD = parseItemsList(config.getString("OlympiadLoserReward", "none"));
		OLYMPIAD_COMP_RITEM = config.getInt("OlympiadCompRewItem", 45584);
		OLYMPIAD_MIN_MATCHES = config.getInt("OlympiadMinMatchesForPoints", 10);
		OLYMPIAD_MARK_PER_POINT = config.getInt("OlympiadMarkPerPoint", 20);
		OLYMPIAD_HERO_POINTS = config.getInt("OlympiadHeroPoints", 30);
		OLYMPIAD_RANK1_POINTS = config.getInt("OlympiadRank1Points", 60);
		OLYMPIAD_RANK2_POINTS = config.getInt("OlympiadRank2Points", 50);
		OLYMPIAD_RANK3_POINTS = config.getInt("OlympiadRank3Points", 45);
		OLYMPIAD_RANK4_POINTS = config.getInt("OlympiadRank4Points", 40);
		OLYMPIAD_RANK5_POINTS = config.getInt("OlympiadRank5Points", 30);
		OLYMPIAD_MAX_POINTS = config.getInt("OlympiadMaxPoints", 10);
		OLYMPIAD_DIVIDER_CLASSED = config.getInt("OlympiadDividerClassed", 5);
		OLYMPIAD_DIVIDER_NON_CLASSED = config.getInt("OlympiadDividerNonClassed", 5);
		OLYMPIAD_MAX_WEEKLY_MATCHES = config.getInt("OlympiadMaxWeeklyMatches", 30);
		OLYMPIAD_LOG_FIGHTS = config.getBoolean("OlympiadLogFights", false);
		OLYMPIAD_SHOW_MONTHLY_WINNERS = config.getBoolean("OlympiadShowMonthlyWinners", true);
		OLYMPIAD_ANNOUNCE_GAMES = config.getBoolean("OlympiadAnnounceGames", true);
		String olyRestrictedItems = config.getString("OlympiadRestrictedItems", "").trim();
		if (!olyRestrictedItems.isEmpty())
		{
			String[] olyRestrictedItemsSplit = olyRestrictedItems.split(",");
			LIST_OLY_RESTRICTED_ITEMS = new HashSet<>(olyRestrictedItemsSplit.length);

			for (String id : olyRestrictedItemsSplit)
			{
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
		}
		else
		{
			LIST_OLY_RESTRICTED_ITEMS.clear();
		}

		OLYMPIAD_WEAPON_ENCHANT_LIMIT = config.getInt("OlympiadWeaponEnchantLimit", -1);
		OLYMPIAD_ARMOR_ENCHANT_LIMIT = config.getInt("OlympiadArmorEnchantLimit", -1);
		OLYMPIAD_WAIT_TIME = config.getInt("OlympiadWaitTime", 60);
		OLYMPIAD_PERIOD = config.getString("OlympiadPeriod", "MONTH");
		OLYMPIAD_PERIOD_MULTIPLIER = config.getInt("OlympiadPeriodMultiplier", 1);
		OLYMPIAD_COMPETITION_DAYS = new ArrayList<>();

		for (String s : config.getString("OlympiadCompetitionDays", "6,7").split(","))
		{
			OLYMPIAD_COMPETITION_DAYS.add(Integer.parseInt(s));
		}

		OLYMPIAD_HIDE_NAMES = config.getBoolean("OlympiadHideNames", true);
	}

	private static List<ItemHolder> parseItemsList(String line)
	{
		if (line.isEmpty())
		{
			return Collections.emptyList();
		}
		String[] propertySplit = line.split(";");
		if (!line.equalsIgnoreCase("none") && propertySplit.length != 0)
		{
			List<ItemHolder> result = new ArrayList<>(propertySplit.length);

			for (String value : propertySplit)
			{
				String[] valueSplit = value.split(",");
				if (valueSplit.length != 2)
				{
					LOGGER.warning("parseItemsList[OlympiadConfig.load()]: invalid entry -> " + valueSplit[0] + ", should be itemId,itemNumber. Skipping to the next entry in the list.");
				}
				else
				{
					int itemId = -1;

					try
					{
						itemId = Integer.parseInt(valueSplit[0]);
					}
					catch (NumberFormatException var12)
					{
						LOGGER.warning("parseItemsList[OlympiadConfig.load()]: invalid itemId -> " + valueSplit[0] + ", value must be an integer. Skipping to the next entry in the list.");
						continue;
					}

					int count = -1;

					try
					{
						count = Integer.parseInt(valueSplit[1]);
					}
					catch (NumberFormatException var11)
					{
						LOGGER.warning("parseItemsList[OlympiadConfig.load()]: invalid item number -> " + valueSplit[1] + ", value must be an integer. Skipping to the next entry in the list.");
						continue;
					}

					if (itemId > 0 && count > 0)
					{
						result.add(new ItemHolder(itemId, count));
					}
				}
			}

			return result;
		}
		return Collections.emptyList();
	}
}
