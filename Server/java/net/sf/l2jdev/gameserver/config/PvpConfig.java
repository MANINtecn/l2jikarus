package net.sf.l2jdev.gameserver.config;

import java.util.Arrays;

import net.sf.l2jdev.commons.util.ConfigReader;

public class PvpConfig
{
	public static final String PVP_CONFIG_FILE = "./config/PVP.ini";
	public static boolean KARMA_DROP_GM;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	public static boolean ANTIFEED_ENABLE;
	public static boolean ANTIFEED_DUALBOX;
	public static boolean ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int ANTIFEED_INTERVAL;
	public static boolean VAMPIRIC_ATTACK_AFFECTS_PVP;
	public static boolean MP_VAMPIRIC_ATTACK_AFFECTS_PVP;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static int MAX_REPUTATION;
	public static int REPUTATION_INCREASE;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/PVP.ini");
		KARMA_DROP_GM = config.getBoolean("CanGMDropEquipment", false);
		KARMA_PK_LIMIT = config.getInt("MinimumPKRequiredToDrop", 4);
		KARMA_NONDROPPABLE_PET_ITEMS = config.getString("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
		KARMA_NONDROPPABLE_ITEMS = config.getString("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");
		String[] karma = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
		KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[karma.length];

		for (int i = 0; i < karma.length; i++)
		{
			KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(karma[i]);
		}

		Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
		karma = KARMA_NONDROPPABLE_ITEMS.split(",");
		KARMA_LIST_NONDROPPABLE_ITEMS = new int[karma.length];

		for (int i = 0; i < karma.length; i++)
		{
			KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(karma[i]);
		}

		Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
		ANTIFEED_ENABLE = config.getBoolean("AntiFeedEnable", false);
		ANTIFEED_DUALBOX = config.getBoolean("AntiFeedDualbox", true);
		ANTIFEED_DISCONNECTED_AS_DUALBOX = config.getBoolean("AntiFeedDisconnectedAsDualbox", true);
		ANTIFEED_INTERVAL = config.getInt("AntiFeedInterval", 120) * 1000;
		VAMPIRIC_ATTACK_AFFECTS_PVP = config.getBoolean("VampiricAttackAffectsPvP", false);
		MP_VAMPIRIC_ATTACK_AFFECTS_PVP = config.getBoolean("MpVampiricAttackAffectsPvP", false);
		PVP_NORMAL_TIME = config.getInt("PvPVsNormalTime", 120000);
		PVP_PVP_TIME = config.getInt("PvPVsPvPTime", 60000);
		MAX_REPUTATION = config.getInt("MaxReputation", 500);
		REPUTATION_INCREASE = config.getInt("ReputationIncrease", 100);
	}
}
