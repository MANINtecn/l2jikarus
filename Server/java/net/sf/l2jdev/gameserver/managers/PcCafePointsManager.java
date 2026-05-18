package net.sf.l2jdev.gameserver.managers;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class PcCafePointsManager
{
	public void run(Player player)
	{
		if (PremiumSystemConfig.PC_CAFE_ENABLED && PremiumSystemConfig.PC_CAFE_RETAIL_LIKE && player.hasEnteredWorld())
		{
			ThreadPool.scheduleAtFixedRate(() -> this.giveRetailPcCafePont(player), PremiumSystemConfig.PC_CAFE_REWARD_TIME, PremiumSystemConfig.PC_CAFE_REWARD_TIME);
		}
	}

	public void giveRetailPcCafePont(Player player)
	{
		if (PremiumSystemConfig.PC_CAFE_ENABLED && PremiumSystemConfig.PC_CAFE_RETAIL_LIKE && player.isOnlineInt() != 0 && (player.hasPremiumStatus() || !PremiumSystemConfig.PC_CAFE_ONLY_PREMIUM) && !player.isInOfflineMode())
		{
			int points = PremiumSystemConfig.ACQUISITION_PC_CAFE_RETAIL_LIKE_POINTS;
			if (points >= PremiumSystemConfig.PC_CAFE_MAX_POINTS)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EARNED_THE_MAXIMUM_NUMBER_OF_ABUNDANCE_POINTS);
			}
			else
			{
				if (PremiumSystemConfig.PC_CAFE_RANDOM_POINT)
				{
					points = Rnd.get(points / 2, points);
				}

				SystemMessage message = null;
				if (PremiumSystemConfig.PC_CAFE_ENABLE_DOUBLE_POINTS && Rnd.get(100) < PremiumSystemConfig.PC_CAFE_DOUBLE_POINTS_CHANCE)
				{
					points *= 2;
					message = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_HAVE_EARNED_ABUNDANCE_POINTS_X_S1);
				}
				else
				{
					message = new SystemMessage(SystemMessageId.YOU_HAVE_RECEIVED_S1_POINT_S_AS_A_DAILY_REWARD_FOR_USING_BLESSING_OF_ABUNDANCE);
				}

				if (player.getPcCafePoints() + points > PremiumSystemConfig.PC_CAFE_MAX_POINTS)
				{
					points = PremiumSystemConfig.PC_CAFE_MAX_POINTS - player.getPcCafePoints();
				}

				message.addLong(points);
				player.sendPacket(message);
				player.setPcCafePoints(player.getPcCafePoints() + points);
				player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), points, 1));
			}
		}
	}

	public void givePcCafePoint(Player player, double exp)
	{
		if (!PremiumSystemConfig.PC_CAFE_RETAIL_LIKE && PremiumSystemConfig.PC_CAFE_ENABLED && !player.isInsideZone(ZoneId.PEACE) && !player.isInsideZone(ZoneId.PVP) && !player.isInsideZone(ZoneId.SIEGE) && player.isOnlineInt() != 0 && !player.isJailed())
		{
			if (!PremiumSystemConfig.PC_CAFE_ONLY_PREMIUM || player.hasPremiumStatus())
			{
				if (!PremiumSystemConfig.PC_CAFE_ONLY_VIP || player.getVipTier() > 0)
				{
					if (player.getPcCafePoints() >= PremiumSystemConfig.PC_CAFE_MAX_POINTS)
					{
						SystemMessage message = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_THE_MAXIMUM_NUMBER_OF_ABUNDANCE_POINTS);
						player.sendPacket(message);
					}
					else
					{
						int points = (int) (exp * 1.0E-4 * PremiumSystemConfig.PC_CAFE_POINT_RATE);
						if (PremiumSystemConfig.PC_CAFE_RANDOM_POINT)
						{
							points = Rnd.get(points / 2, points);
						}

						if (points == 0 && exp > 0.0 && PremiumSystemConfig.PC_CAFE_REWARD_LOW_EXP_KILLS && Rnd.get(100) < PremiumSystemConfig.PC_CAFE_LOW_EXP_KILLS_CHANCE)
						{
							points = 1;
						}

						if (points > 0)
						{
							SystemMessage message = null;
							if (PremiumSystemConfig.PC_CAFE_ENABLE_DOUBLE_POINTS && Rnd.get(100) < PremiumSystemConfig.PC_CAFE_DOUBLE_POINTS_CHANCE)
							{
								points *= 2;
								message = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_HAVE_EARNED_ABUNDANCE_POINTS_X_S1);
							}
							else
							{
								message = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_HAVE_EARNED_ABUNDANCE_POINTS_X_S1);
							}

							if (player.getPcCafePoints() + points > PremiumSystemConfig.PC_CAFE_MAX_POINTS)
							{
								points = PremiumSystemConfig.PC_CAFE_MAX_POINTS - player.getPcCafePoints();
							}

							message.addLong(points);
							player.sendPacket(message);
							player.setPcCafePoints(player.getPcCafePoints() + points);
							player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), points, 1));
						}
					}
				}
			}
		}
	}

	public static PcCafePointsManager getInstance()
	{
		return PcCafePointsManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PcCafePointsManager INSTANCE = new PcCafePointsManager();
	}
}
