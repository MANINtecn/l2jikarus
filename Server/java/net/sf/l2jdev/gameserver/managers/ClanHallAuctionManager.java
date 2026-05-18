package net.sf.l2jdev.gameserver.managers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.residences.ClanHallAuction;

public class ClanHallAuctionManager
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallAuctionManager.class.getName());
	private static final Map<Integer, ClanHallAuction> AUCTIONS = new HashMap<>();
	private static ScheduledFuture<?> _endTask;

	protected ClanHallAuctionManager()
	{
		long currentTime = System.currentTimeMillis();
		Calendar start = Calendar.getInstance();
		start.set(7, 4);
		start.set(11, 19);
		start.set(12, 0);
		start.set(13, 0);
		if (start.getTimeInMillis() < currentTime)
		{
			start.add(6, 1);

			while (start.get(7) != 4)
			{
				start.add(6, 1);
			}
		}

		long startDelay = Math.max(0L, start.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onStart, startDelay, 604800000L);
		if (startDelay > 0L)
		{
			this.onStart();
		}

		Calendar end = Calendar.getInstance();
		end.set(7, 4);
		end.set(11, 11);
		end.set(12, 0);
		end.set(13, 0);
		if (end.getTimeInMillis() < currentTime)
		{
			end.add(6, 1);

			while (end.get(7) != 4)
			{
				end.add(6, 1);
			}
		}

		long endDelay = Math.max(0L, end.getTimeInMillis() - currentTime);
		_endTask = ThreadPool.scheduleAtFixedRate(this::onEnd, endDelay, 604800000L);
	}

	private void onStart()
	{
		LOGGER.info(this.getClass().getSimpleName() + ": Clan Hall Auction has started!");
		AUCTIONS.clear();
		ClanHallData.getInstance().getFreeAuctionableHall().forEach(c -> AUCTIONS.put(c.getResidenceId(), new ClanHallAuction(c.getResidenceId())));
	}

	private void onEnd()
	{
		AUCTIONS.values().forEach(ClanHallAuction::finalizeAuctions);
		AUCTIONS.clear();
		LOGGER.info(this.getClass().getSimpleName() + ": Clan Hall Auction has ended!");
	}

	public ClanHallAuction getClanHallAuctionById(int clanHallId)
	{
		return AUCTIONS.get(clanHallId);
	}

	public ClanHallAuction getClanHallAuctionByClan(Clan clan)
	{
		for (ClanHallAuction auction : AUCTIONS.values())
		{
			if (auction.getBids().containsKey(clan.getId()))
			{
				return auction;
			}
		}

		return null;
	}

	public boolean checkForClanBid(int clanHallId, Clan clan)
	{
		for (Entry<Integer, ClanHallAuction> auction : AUCTIONS.entrySet())
		{
			if (auction.getKey() != clanHallId && auction.getValue().getBids().containsKey(clan.getId()))
			{
				return true;
			}
		}

		return false;
	}

	public long getRemainingTime()
	{
		return _endTask.getDelay(TimeUnit.MILLISECONDS);
	}

	public static ClanHallAuctionManager getInstance()
	{
		return ClanHallAuctionManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ClanHallAuctionManager INSTANCE = new ClanHallAuctionManager();
	}
}
