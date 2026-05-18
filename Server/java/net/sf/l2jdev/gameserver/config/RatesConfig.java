package net.sf.l2jdev.gameserver.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.ConfigReader;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.DropType;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.DropHolder;

public class RatesConfig
{
	private static final Logger LOGGER = Logger.getLogger(RatesConfig.class.getName());
	public static final String RATES_CONFIG_FILE = "./config/Rates.ini";
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_INSTANCE_XP;
	public static float RATE_INSTANCE_SP;
	public static float RATE_INSTANCE_PARTY_XP;
	public static float RATE_INSTANCE_PARTY_SP;
	public static float RATE_EXTRACTABLE;
	public static int RATE_DROP_MANOR;
	public static float QUEST_ITEM_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static float RATE_RAIDBOSS_POINTS;
	public static float RATE_VITALITY_EXP_MULTIPLIER;
	public static float RATE_LIMITED_SAYHA_GRACE_EXP_MULTIPLIER;
	public static int VITALITY_MAX_ITEMS_ALLOWED;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_KARMA_LOST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static float RATE_DEATH_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_SPOIL_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_HERB_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_RAID_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_DEATH_DROP_CHANCE_MULTIPLIER;
	public static float RATE_SPOIL_DROP_CHANCE_MULTIPLIER;
	public static float RATE_HERB_DROP_CHANCE_MULTIPLIER;
	public static float RATE_RAID_DROP_CHANCE_MULTIPLIER;
	public static Map<Integer, Float> RATE_DROP_AMOUNT_BY_ID;
	public static Map<Integer, Float> RATE_DROP_CHANCE_BY_ID;
	public static int DROP_MAX_OCCURRENCES_NORMAL;
	public static int DROP_MAX_OCCURRENCES_RAIDBOSS;
	public static int DROP_ADENA_MAX_LEVEL_LOWEST_DIFFERENCE;
	public static int DROP_ITEM_MAX_LEVEL_LOWEST_DIFFERENCE;
	public static int EVENT_ITEM_MAX_LEVEL_LOWEST_DIFFERENCE;
	public static double BLESSING_CHANCE;
	public static boolean BOSS_DROP_ENABLED;
	public static int BOSS_DROP_MIN_LEVEL;
	public static int BOSS_DROP_MAX_LEVEL;
	public static List<DropHolder> BOSS_DROP_LIST = new ArrayList<>();
	public static boolean LCOIN_DROP_ENABLED;
	public static double LCOIN_DROP_CHANCE;
	public static int LCOIN_MIN_MOB_LEVEL;
	public static int LCOIN_MIN_QUANTITY;
	public static int LCOIN_MAX_QUANTITY;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Rates.ini");
		RATE_XP = config.getFloat("RateXp", 1.0F);
		RATE_SP = config.getFloat("RateSp", 1.0F);
		RATE_PARTY_XP = config.getFloat("RatePartyXp", 1.0F);
		RATE_PARTY_SP = config.getFloat("RatePartySp", 1.0F);
		RATE_INSTANCE_XP = config.getFloat("RateInstanceXp", -1.0F);
		if (RATE_INSTANCE_XP < 0.0F)
		{
			RATE_INSTANCE_XP = RATE_XP;
		}

		RATE_INSTANCE_SP = config.getFloat("RateInstanceSp", -1.0F);
		if (RATE_INSTANCE_SP < 0.0F)
		{
			RATE_INSTANCE_SP = RATE_SP;
		}

		RATE_INSTANCE_PARTY_XP = config.getFloat("RateInstancePartyXp", -1.0F);
		if (RATE_INSTANCE_PARTY_XP < 0.0F)
		{
			RATE_INSTANCE_PARTY_XP = RATE_PARTY_XP;
		}

		RATE_INSTANCE_PARTY_SP = config.getFloat("RateInstancePartySp", -1.0F);
		if (RATE_INSTANCE_PARTY_SP < 0.0F)
		{
			RATE_INSTANCE_PARTY_SP = RATE_PARTY_SP;
		}

		RATE_EXTRACTABLE = config.getFloat("RateExtractable", 1.0F);
		RATE_DROP_MANOR = config.getInt("RateDropManor", 1);
		QUEST_ITEM_DROP_AMOUNT_MULTIPLIER = config.getFloat("QuestItemDropAmountMultiplier", 1.0F);
		RATE_QUEST_REWARD = config.getFloat("RateQuestReward", 1.0F);
		RATE_QUEST_REWARD_XP = config.getFloat("RateQuestRewardXP", 1.0F);
		RATE_QUEST_REWARD_SP = config.getFloat("RateQuestRewardSP", 1.0F);
		RATE_QUEST_REWARD_ADENA = config.getFloat("RateQuestRewardAdena", 1.0F);
		RATE_QUEST_REWARD_USE_MULTIPLIERS = config.getBoolean("UseQuestRewardMultipliers", false);
		RATE_QUEST_REWARD_POTION = config.getFloat("RateQuestRewardPotion", 1.0F);
		RATE_QUEST_REWARD_SCROLL = config.getFloat("RateQuestRewardScroll", 1.0F);
		RATE_QUEST_REWARD_RECIPE = config.getFloat("RateQuestRewardRecipe", 1.0F);
		RATE_QUEST_REWARD_MATERIAL = config.getFloat("RateQuestRewardMaterial", 1.0F);
		RATE_RAIDBOSS_POINTS = config.getFloat("RateRaidbossPointsReward", 1.0F);
		RATE_VITALITY_EXP_MULTIPLIER = config.getFloat("RateVitalityExpMultiplier", 2.0F);
		RATE_LIMITED_SAYHA_GRACE_EXP_MULTIPLIER = config.getFloat("RateLimitedSayhaGraceExpMultiplier", 2.0F);
		VITALITY_MAX_ITEMS_ALLOWED = config.getInt("VitalityMaxItemsAllowed", 999);
		if (VITALITY_MAX_ITEMS_ALLOWED == 0)
		{
			VITALITY_MAX_ITEMS_ALLOWED = Integer.MAX_VALUE;
		}

		RATE_VITALITY_LOST = config.getFloat("RateVitalityLost", 1.0F);
		RATE_VITALITY_GAIN = config.getFloat("RateVitalityGain", 1.0F);
		RATE_KARMA_LOST = config.getFloat("RateKarmaLost", -1.0F);
		if (RATE_KARMA_LOST == -1.0F)
		{
			RATE_KARMA_LOST = RATE_XP;
		}

		RATE_KARMA_EXP_LOST = config.getFloat("RateKarmaExpLost", 1.0F);
		RATE_SIEGE_GUARDS_PRICE = config.getFloat("RateSiegeGuardsPrice", 1.0F);
		PLAYER_DROP_LIMIT = config.getInt("PlayerDropLimit", 3);
		PLAYER_RATE_DROP = config.getInt("PlayerRateDrop", 5);
		PLAYER_RATE_DROP_ITEM = config.getInt("PlayerRateDropItem", 70);
		PLAYER_RATE_DROP_EQUIP = config.getInt("PlayerRateDropEquip", 25);
		PLAYER_RATE_DROP_EQUIP_WEAPON = config.getInt("PlayerRateDropEquipWeapon", 5);
		PET_XP_RATE = config.getFloat("PetXpRate", 1.0F);
		PET_FOOD_RATE = config.getInt("PetFoodRate", 1);
		SINEATER_XP_RATE = config.getFloat("SinEaterXpRate", 1.0F);
		KARMA_DROP_LIMIT = config.getInt("KarmaDropLimit", 10);
		KARMA_RATE_DROP = config.getInt("KarmaRateDrop", 70);
		KARMA_RATE_DROP_ITEM = config.getInt("KarmaRateDropItem", 50);
		KARMA_RATE_DROP_EQUIP = config.getInt("KarmaRateDropEquip", 40);
		KARMA_RATE_DROP_EQUIP_WEAPON = config.getInt("KarmaRateDropEquipWeapon", 10);
		RATE_DEATH_DROP_AMOUNT_MULTIPLIER = config.getFloat("DeathDropAmountMultiplier", 1.0F);
		RATE_SPOIL_DROP_AMOUNT_MULTIPLIER = config.getFloat("SpoilDropAmountMultiplier", 1.0F);
		RATE_HERB_DROP_AMOUNT_MULTIPLIER = config.getFloat("HerbDropAmountMultiplier", 1.0F);
		RATE_RAID_DROP_AMOUNT_MULTIPLIER = config.getFloat("RaidDropAmountMultiplier", 1.0F);
		RATE_DEATH_DROP_CHANCE_MULTIPLIER = config.getFloat("DeathDropChanceMultiplier", 1.0F);
		RATE_SPOIL_DROP_CHANCE_MULTIPLIER = config.getFloat("SpoilDropChanceMultiplier", 1.0F);
		RATE_HERB_DROP_CHANCE_MULTIPLIER = config.getFloat("HerbDropChanceMultiplier", 1.0F);
		RATE_RAID_DROP_CHANCE_MULTIPLIER = config.getFloat("RaidDropChanceMultiplier", 1.0F);
		String[] dropAmountMultiplier = config.getString("DropAmountMultiplierByItemId", "").split(";");
		RATE_DROP_AMOUNT_BY_ID = new HashMap<>(dropAmountMultiplier.length);
		if (!dropAmountMultiplier[0].isEmpty())
		{
			for (String item : dropAmountMultiplier)
			{
				String[] itemSplit = item.split(",");
				if (itemSplit.length != 2)
				{
					LOGGER.warning(StringUtil.concat("Config.load(): invalid config property -> DropAmountMultiplierByItemId \"", item, "\""));
				}
				else
				{
					try
					{
						RATE_DROP_AMOUNT_BY_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
					}
					catch (NumberFormatException var10)
					{
						if (!item.isEmpty())
						{
							LOGGER.warning(StringUtil.concat("Config.load(): invalid config property -> DropAmountMultiplierByItemId \"", item, "\""));
						}
					}
				}
			}
		}

		String[] dropChanceMultiplier = config.getString("DropChanceMultiplierByItemId", "").split(";");
		RATE_DROP_CHANCE_BY_ID = new HashMap<>(dropChanceMultiplier.length);
		if (!dropChanceMultiplier[0].isEmpty())
		{
			for (String itemx : dropChanceMultiplier)
			{
				String[] itemSplit = itemx.split(",");
				if (itemSplit.length != 2)
				{
					LOGGER.warning(StringUtil.concat("Config.load(): invalid config property -> DropChanceMultiplierByItemId \"", itemx, "\""));
				}
				else
				{
					try
					{
						RATE_DROP_CHANCE_BY_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
					}
					catch (NumberFormatException var9)
					{
						if (!itemx.isEmpty())
						{
							LOGGER.warning(StringUtil.concat("Config.load(): invalid config property -> DropChanceMultiplierByItemId \"", itemx, "\""));
						}
					}
				}
			}
		}

		DROP_MAX_OCCURRENCES_NORMAL = config.getInt("DropMaxOccurrencesNormal", 2);
		DROP_MAX_OCCURRENCES_RAIDBOSS = config.getInt("DropMaxOccurrencesRaidboss", 7);
		DROP_ADENA_MAX_LEVEL_LOWEST_DIFFERENCE = config.getInt("DropAdenaMaxLevelLowestDifference", 14);
		DROP_ITEM_MAX_LEVEL_LOWEST_DIFFERENCE = config.getInt("DropItemMaxLevelLowestDifference", 14);
		EVENT_ITEM_MAX_LEVEL_LOWEST_DIFFERENCE = config.getInt("EventItemMaxLevelLowestDifference", 14);
		BLESSING_CHANCE = config.getDouble("BlessingChance", 15.0);
		BOSS_DROP_ENABLED = config.getBoolean("BossDropEnable", false);
		BOSS_DROP_MIN_LEVEL = config.getInt("BossDropMinLevel", 40);
		BOSS_DROP_MAX_LEVEL = config.getInt("BossDropMaxLevel", 999);
		BOSS_DROP_LIST.clear();

		for (String s : config.getString("BossDropList", "").trim().split(";"))
		{
			if (!s.isEmpty())
			{
				BOSS_DROP_LIST.add(new DropHolder(DropType.DROP, Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]), Integer.parseInt(s.split(",")[2]), Double.parseDouble(s.split(",")[3])));
			}
		}

		LCOIN_DROP_ENABLED = config.getBoolean("LCoinDropEnable", false);
		LCOIN_DROP_CHANCE = config.getDouble("LCoinDropChance", 15.0);
		LCOIN_MIN_MOB_LEVEL = config.getInt("LCoinMinimumMonsterLevel", 40);
		LCOIN_MIN_QUANTITY = config.getInt("LCoinMinDropQuantity", 1);
		LCOIN_MAX_QUANTITY = config.getInt("LCoinMaxDropQuantity", 5);
	}
}
