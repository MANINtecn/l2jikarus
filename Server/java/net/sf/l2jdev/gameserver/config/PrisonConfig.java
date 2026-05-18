package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class PrisonConfig
{
	public static final String PRISON_CONFIG_FILE = "./config/Prison.ini";
	public static boolean ENABLE_PRISON;
	public static long REPUTATION_FOR_ZONE_1;
	public static int PK_FOR_ZONE_1;
	public static int PK_FOR_ZONE_2;
	public static long SENTENCE_TIME_ZONE_1;
	public static long SENTENCE_TIME_ZONE_2;
	public static Location ENTRANCE_LOC_ZONE_1;
	public static Location ENTRANCE_LOC_ZONE_2;
	public static Location RELEASE_LOC_ZONE_1;
	public static Location RELEASE_LOC_ZONE_2;
	public static int MARK_RELEASE_AMOUNT;
	public static long LCOIN_RELEASE_AMOUNT;
	public static int REP_POINTS_RECEIVED_BY_ZONE_1;
	public static int REP_POINTS_RECEIVED_BY_ZONE_2;
	public static ItemHolder BAIL_ZONE_1;
	public static ItemHolder BAIL_ZONE_2;
	public static ItemHolder DONATION_BAIL_ZONE_1;
	public static ItemHolder DONATION_BAIL_ZONE_2;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Prison.ini");
		ENABLE_PRISON = config.getBoolean("Enable", false);
		REPUTATION_FOR_ZONE_1 = config.getLong("ReputationForZone1", -150000L);
		PK_FOR_ZONE_1 = config.getInt("PKsForZone1", 1);
		PK_FOR_ZONE_2 = config.getInt("PKsForZone2", 50);
		SENTENCE_TIME_ZONE_1 = config.getLong("SentenceTimeZone1", 1440L) * 60000L;
		SENTENCE_TIME_ZONE_2 = config.getLong("SentenceTimeZone2", 4320L) * 60000L;
		String[] entrance1 = config.getString("EntranceLocZone1", "-77998,-52649,-11494").split(",");
		ENTRANCE_LOC_ZONE_1 = new Location(Integer.parseInt(entrance1[0]), Integer.parseInt(entrance1[1]), Integer.parseInt(entrance1[2]));
		String[] entrance2 = config.getString("EntranceLocZone2", "-77998,-52649,-11494").split(",");
		ENTRANCE_LOC_ZONE_2 = new Location(Integer.parseInt(entrance2[0]), Integer.parseInt(entrance2[1]), Integer.parseInt(entrance2[2]));
		String[] release1 = config.getString("ReleaseLocZone1", "83401,148645,-3380").split(",");
		RELEASE_LOC_ZONE_1 = new Location(Integer.parseInt(release1[0]), Integer.parseInt(release1[1]), Integer.parseInt(release1[2]));
		String[] release2 = config.getString("ReleaseLocZone2", "83401,148645,-3380").split(",");
		RELEASE_LOC_ZONE_2 = new Location(Integer.parseInt(release2[0]), Integer.parseInt(release2[1]), Integer.parseInt(release2[2]));
		MARK_RELEASE_AMOUNT = config.getInt("MarkReleaseAmount", 100);
		LCOIN_RELEASE_AMOUNT = config.getLong("LCoinReleaseAmount", 10000L);
		REP_POINTS_RECEIVED_BY_ZONE_1 = config.getInt("RepPointsReceivedByZone1", 100);
		REP_POINTS_RECEIVED_BY_ZONE_2 = config.getInt("RepPointsReceivedByZone2", 200);
		String[] bail1 = config.getString("BailZone1", "57,2000000").split(",");
		BAIL_ZONE_1 = new ItemHolder(Integer.parseInt(bail1[0]), Long.parseLong(bail1[1]));
		String[] bail2 = config.getString("BailZone2", "82402,108").split(",");
		BAIL_ZONE_2 = new ItemHolder(Integer.parseInt(bail2[0]), Long.parseLong(bail2[1]));
		String[] donationBailZone1 = config.getString("DonationBailZone1", "57,1000000000").split(",");
		DONATION_BAIL_ZONE_1 = new ItemHolder(Integer.parseInt(donationBailZone1[0]), Long.parseLong(donationBailZone1[1]));
		String[] donationBailZone2 = config.getString("DonationBailZone2", "57,1500000000").split(",");
		DONATION_BAIL_ZONE_2 = new ItemHolder(Integer.parseInt(donationBailZone2[0]), Long.parseLong(donationBailZone2[1]));
	}
}
