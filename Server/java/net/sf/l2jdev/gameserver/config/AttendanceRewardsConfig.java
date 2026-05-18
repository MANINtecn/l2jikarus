package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class AttendanceRewardsConfig
{
	public static final String ATTENDANCE_CONFIG_FILE = "./config/AttendanceRewards.ini";
	public static boolean ENABLE_ATTENDANCE_REWARDS;
	public static boolean PREMIUM_ONLY_ATTENDANCE_REWARDS;
	public static boolean VIP_ONLY_ATTENDANCE_REWARDS;
	public static boolean ATTENDANCE_REWARDS_SHARE_ACCOUNT;
	public static int ATTENDANCE_REWARD_DELAY;
	public static boolean ATTENDANCE_POPUP_START;
	public static boolean ATTENDANCE_POPUP_WINDOW;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/AttendanceRewards.ini");
		ENABLE_ATTENDANCE_REWARDS = config.getBoolean("EnableAttendanceRewards", false);
		PREMIUM_ONLY_ATTENDANCE_REWARDS = config.getBoolean("PremiumOnlyAttendanceRewards", false);
		VIP_ONLY_ATTENDANCE_REWARDS = config.getBoolean("VipOnlyAttendanceRewards", false);
		ATTENDANCE_REWARDS_SHARE_ACCOUNT = config.getBoolean("AttendanceRewardsShareAccount", false);
		ATTENDANCE_REWARD_DELAY = config.getInt("AttendanceRewardDelay", 30);
		ATTENDANCE_POPUP_START = config.getBoolean("AttendancePopupStart", true);
		ATTENDANCE_POPUP_WINDOW = config.getBoolean("AttendancePopupWindow", false);
	}
}
