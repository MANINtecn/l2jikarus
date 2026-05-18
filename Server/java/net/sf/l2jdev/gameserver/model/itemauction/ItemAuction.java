package net.sf.l2jdev.gameserver.model.itemauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ItemAuction
{
	private static final Logger LOGGER = Logger.getLogger(ItemAuction.class.getName());
	private static final long ENDING_TIME_EXTEND_5 = TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES);
	private static final long ENDING_TIME_EXTEND_3 = TimeUnit.MILLISECONDS.convert(3L, TimeUnit.MINUTES);
	private final int _auctionId;
	private final int _instanceId;
	private final long _startingTime;
	private volatile long _endingTime;
	private final AuctionItem _auctionItem;
	private final List<ItemAuctionBid> _auctionBids;
	private final Object _auctionStateLock;
	private ItemAuctionState _auctionState;
	private ItemAuctionExtendState _scheduledAuctionEndingExtendState;
	private ItemAuctionExtendState _auctionEndingExtendState;
	private final ItemInfo _itemInfo;
	private ItemAuctionBid _highestBid;
	private int _lastBidPlayerObjId;
	public static final String DELETE_ITEM_AUCTION_BID = "DELETE FROM item_auction_bid WHERE auctionId = ? AND playerObjId = ?";
	public static final String INSERT_ITEM_AUCTION_BID = "INSERT INTO item_auction_bid (auctionId, playerObjId, playerBid) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playerBid = ?";

	public ItemAuction(int auctionId, int instanceId, long startingTime, long endingTime, AuctionItem auctionItem)
	{
		this(auctionId, instanceId, startingTime, endingTime, auctionItem, new ArrayList<>(), ItemAuctionState.CREATED);
	}

	public ItemAuction(int auctionId, int instanceId, long startingTime, long endingTime, AuctionItem auctionItem, List<ItemAuctionBid> auctionBids, ItemAuctionState auctionState)
	{
		this._auctionId = auctionId;
		this._instanceId = instanceId;
		this._startingTime = startingTime;
		this._endingTime = endingTime;
		this._auctionItem = auctionItem;
		this._auctionBids = auctionBids;
		this._auctionState = auctionState;
		this._auctionStateLock = new Object();
		this._scheduledAuctionEndingExtendState = ItemAuctionExtendState.INITIAL;
		this._auctionEndingExtendState = ItemAuctionExtendState.INITIAL;
		Item item = this._auctionItem.createNewItemInstance();
		this._itemInfo = new ItemInfo(item);
		World.getInstance().removeObject(item);

		for (ItemAuctionBid bid : this._auctionBids)
		{
			if (this._highestBid == null || this._highestBid.getLastBid() < bid.getLastBid())
			{
				this._highestBid = bid;
			}
		}
	}

	public ItemAuctionState getAuctionState()
	{
		synchronized (this._auctionStateLock)
		{
			return this._auctionState;
		}
	}

	public boolean setAuctionState(ItemAuctionState expected, ItemAuctionState wanted)
	{
		synchronized (this._auctionStateLock)
		{
			if (this._auctionState != expected)
			{
				return false;
			}
			this._auctionState = wanted;
			this.storeMe();
			return true;
		}
	}

	public int getAuctionId()
	{
		return this._auctionId;
	}

	public int getInstanceId()
	{
		return this._instanceId;
	}

	public ItemInfo getItemInfo()
	{
		return this._itemInfo;
	}

	public Item createNewItemInstance()
	{
		return this._auctionItem.createNewItemInstance();
	}

	public long getAuctionInitBid()
	{
		return this._auctionItem.getAuctionInitBid();
	}

	public ItemAuctionBid getHighestBid()
	{
		return this._highestBid;
	}

	public ItemAuctionExtendState getAuctionEndingExtendState()
	{
		return this._auctionEndingExtendState;
	}

	public ItemAuctionExtendState getScheduledAuctionEndingExtendState()
	{
		return this._scheduledAuctionEndingExtendState;
	}

	public void setScheduledAuctionEndingExtendState(ItemAuctionExtendState state)
	{
		this._scheduledAuctionEndingExtendState = state;
	}

	public long getStartingTime()
	{
		return this._startingTime;
	}

	public long getEndingTime()
	{
		return this._endingTime;
	}

	public long getStartingTimeRemaining()
	{
		return Math.max(this._endingTime - System.currentTimeMillis(), 0L);
	}

	public long getFinishingTimeRemaining()
	{
		return Math.max(this._endingTime - System.currentTimeMillis(), 0L);
	}

	public void storeMe()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO item_auction (auctionId,instanceId,auctionItemId,startingTime,endingTime,auctionStateId) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE auctionStateId=?");)
		{
			statement.setInt(1, this._auctionId);
			statement.setInt(2, this._instanceId);
			statement.setInt(3, this._auctionItem.getAuctionItemId());
			statement.setLong(4, this._startingTime);
			statement.setLong(5, this._endingTime);
			statement.setByte(6, this._auctionState.getStateId());
			statement.setByte(7, this._auctionState.getStateId());
			statement.execute();
		}
		catch (SQLException var9)
		{
			LOGGER.log(Level.WARNING, "", var9);
		}
	}

	public int getAndSetLastBidPlayerObjectId(int playerObjId)
	{
		int lastBid = this._lastBidPlayerObjId;
		this._lastBidPlayerObjId = playerObjId;
		return lastBid;
	}

	private void updatePlayerBid(ItemAuctionBid bid, boolean delete)
	{
		this.updatePlayerBidInternal(bid, delete);
	}

	private void updatePlayerBidInternal(ItemAuctionBid bid, boolean delete)
	{
		String query = delete ? "DELETE FROM item_auction_bid WHERE auctionId = ? AND playerObjId = ?" : "INSERT INTO item_auction_bid (auctionId, playerObjId, playerBid) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playerBid = ?";

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(query);)
		{
			ps.setInt(1, this._auctionId);
			ps.setInt(2, bid.getPlayerObjId());
			if (!delete)
			{
				ps.setLong(3, bid.getLastBid());
				ps.setLong(4, bid.getLastBid());
			}

			ps.execute();
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.WARNING, "", var12);
		}
	}

	public void registerBid(Player player, long newBid)
	{
		if (player == null)
		{
			throw new NullPointerException();
		}
		else if (newBid < this._auctionItem.getAuctionInitBid())
		{
			player.sendPacket(SystemMessageId.YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_CURRENTLY_BEING_BID);
		}
		else if (newBid > 100000000000L)
		{
			player.sendPacket(SystemMessageId.THE_HIGHEST_BID_IS_OVER_999_9_BILLION_THEREFORE_YOU_CANNOT_PLACE_A_BID);
		}
		else if (this.getAuctionState() == ItemAuctionState.STARTED)
		{
			int playerObjId = player.getObjectId();
			synchronized (this._auctionBids)
			{
				if (this._highestBid != null && newBid < this._highestBid.getLastBid())
				{
					player.sendPacket(SystemMessageId.YOUR_BID_MUST_BE_HIGHER_THAN_THE_CURRENT_HIGHEST_BID);
				}
				else
				{
					ItemAuctionBid bid = this.getBidFor(playerObjId);
					if (bid == null)
					{
						if (!this.reduceItemCount(player, newBid))
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA_FOR_THIS_BID);
							return;
						}

						bid = new ItemAuctionBid(playerObjId, newBid);
						this._auctionBids.add(bid);
					}
					else
					{
						if (!bid.isCanceled())
						{
							if (newBid < bid.getLastBid())
							{
								player.sendPacket(SystemMessageId.YOUR_BID_MUST_BE_HIGHER_THAN_THE_CURRENT_HIGHEST_BID);
								return;
							}

							if (!this.reduceItemCount(player, newBid - bid.getLastBid()))
							{
								player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA_FOR_THIS_BID);
								return;
							}
						}
						else if (!this.reduceItemCount(player, newBid))
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA_FOR_THIS_BID);
							return;
						}

						bid.setLastBid(newBid);
					}

					this.onPlayerBid(player, bid);
					this.updatePlayerBid(bid, false);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUBMITTED_A_BID_FOR_THE_AUCTION_OF_S1);
					sm.addLong(newBid);
					player.sendPacket(sm);
				}
			}
		}
	}

	private void onPlayerBid(Player player, ItemAuctionBid bid)
	{
		if (this._highestBid == null)
		{
			this._highestBid = bid;
		}
		else if (this._highestBid.getLastBid() < bid.getLastBid())
		{
			Player old = this._highestBid.getPlayer();
			if (old != null)
			{
				old.sendPacket(SystemMessageId.YOU_WERE_OUTBID_THE_NEW_HIGHEST_BID_IS_S1_ADENA);
			}

			this._highestBid = bid;
		}

		if (this._endingTime - System.currentTimeMillis() <= 600000L)
		{
			switch (this._auctionEndingExtendState)
			{
				case INITIAL:
					this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_5_MIN;
					this._endingTime = this._endingTime + ENDING_TIME_EXTEND_5;
					this.broadcastToAllBidders(new SystemMessage(SystemMessageId.BIDDER_EXISTS_THE_AUCTION_TIME_HAS_BEEN_EXTENDED_FOR_5_MIN));
					break;
				case EXTEND_BY_5_MIN:
					if (this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_3_MIN;
						this._endingTime = this._endingTime + ENDING_TIME_EXTEND_3;
						this.broadcastToAllBidders(new SystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_HAS_BEEN_EXTENDED_FOR_3_MIN));
					}
					break;
				case EXTEND_BY_3_MIN:
					if (GeneralConfig.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID > 0L && this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
						this._endingTime = this._endingTime + GeneralConfig.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
					}
					break;
				case EXTEND_BY_CONFIG_PHASE_A:
					if (this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId() && this._scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
					{
						this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B;
						this._endingTime = this._endingTime + GeneralConfig.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
					}
					break;
				case EXTEND_BY_CONFIG_PHASE_B:
					if (this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId() && this._scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
					{
						this._endingTime = this._endingTime + GeneralConfig.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
						this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
					}
			}
		}
	}

	public void broadcastToAllBidders(ServerPacket packet)
	{
		ThreadPool.execute(() -> this.broadcastToAllBiddersInternal(packet));
	}

	public void broadcastToAllBiddersInternal(ServerPacket packet)
	{
		int i = this._auctionBids.size();

		while (i-- > 0)
		{
			ItemAuctionBid bid = this._auctionBids.get(i);
			if (bid != null)
			{
				Player player = bid.getPlayer();
				if (player != null)
				{
					player.sendPacket(packet);
				}
			}
		}
	}

	public boolean cancelBid(Player player)
	{
		if (player == null)
		{
			throw new NullPointerException();
		}
		switch (this.getAuctionState())
		{
			case CREATED:
				return false;
			case FINISHED:
				if (this._startingTime < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(GeneralConfig.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))
				{
					return false;
				}
			default:
				int playerObjId = player.getObjectId();
				synchronized (this._auctionBids)
				{
					if (this._highestBid == null)
					{
						return false;
					}
					int bidIndex = this.getBidIndexFor(playerObjId);
					if (bidIndex == -1)
					{
						return false;
					}
					ItemAuctionBid bid = this._auctionBids.get(bidIndex);
					if (bid.getPlayerObjId() == this._highestBid.getPlayerObjId())
					{
						if (this.getAuctionState() == ItemAuctionState.FINISHED)
						{
							return false;
						}
						player.sendPacket(SystemMessageId.YOU_CURRENTLY_HAVE_THE_HIGHEST_BID);
						return true;
					}
					else if (bid.isCanceled())
					{
						return false;
					}
					else
					{
						this.increaseItemCount(player, bid.getLastBid());
						bid.cancelBid();
						this.updatePlayerBid(bid, this.getAuctionState() == ItemAuctionState.FINISHED);
						player.sendPacket(SystemMessageId.YOU_HAVE_CANCELLED_YOUR_BID);
						return true;
					}
				}
		}
	}

	public void clearCanceledBids()
	{
		if (this.getAuctionState() != ItemAuctionState.FINISHED)
		{
			throw new IllegalStateException("Attempt to clear canceled bids for non-finished auction");
		}
		synchronized (this._auctionBids)
		{
			for (ItemAuctionBid bid : this._auctionBids)
			{
				if (bid != null && bid.isCanceled())
				{
					this.updatePlayerBid(bid, true);
				}
			}
		}
	}

	public boolean reduceItemCount(Player player, long count)
	{
		if (!player.reduceAdena(ItemProcessType.FEE, count, player, true))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA_FOR_THIS_BID);
			return false;
		}
		return true;
	}

	public void increaseItemCount(Player player, long count)
	{
		player.addAdena(ItemProcessType.REFUND, count, player, true);
	}

	public long getLastBid(Player player)
	{
		ItemAuctionBid bid = this.getBidFor(player.getObjectId());
		return bid != null ? bid.getLastBid() : -1L;
	}

	public ItemAuctionBid getBidFor(int playerObjId)
	{
		int index = this.getBidIndexFor(playerObjId);
		return index != -1 ? this._auctionBids.get(index) : null;
	}

	private int getBidIndexFor(int playerObjId)
	{
		int i = this._auctionBids.size();

		while (i-- > 0)
		{
			ItemAuctionBid bid = this._auctionBids.get(i);
			if (bid != null && bid.getPlayerObjId() == playerObjId)
			{
				return i;
			}
		}

		return -1;
	}
}
