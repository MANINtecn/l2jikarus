package net.sf.l2jdev.gameserver.model.itemauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.managers.ItemAuctionManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ItemAuctionInstance
{
	protected static final Logger LOGGER = Logger.getLogger(ItemAuctionInstance.class.getName());
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
	private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
	private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);
	public static final String SELECT_AUCTION_ID_BY_INSTANCE_ID = "SELECT auctionId FROM item_auction WHERE instanceId = ?";
	public static final String SELECT_AUCTION_INFO = "SELECT auctionItemId, startingTime, endingTime, auctionStateId FROM item_auction WHERE auctionId = ? ";
	public static final String DELETE_AUCTION_INFO_BY_AUCTION_ID = "DELETE FROM item_auction WHERE auctionId = ?";
	public static final String DELETE_AUCTION_BID_INFO_BY_AUCTION_ID = "DELETE FROM item_auction_bid WHERE auctionId = ?";
	public static final String SELECT_PLAYERS_ID_BY_AUCTION_ID = "SELECT playerObjId, playerBid FROM item_auction_bid WHERE auctionId = ?";
	private final int _instanceId;
	private final AtomicInteger _auctionIds;
	private final Map<Integer, ItemAuction> _auctions;
	private final List<AuctionItem> _items;
	private final AuctionDateGenerator _dateGenerator;
	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;
	private ScheduledFuture<?> _stateTask;

	public ItemAuctionInstance(int instanceId, AtomicInteger auctionIds, Node node) throws Exception
	{
		this._instanceId = instanceId;
		this._auctionIds = auctionIds;
		this._auctions = new HashMap<>();
		this._items = new ArrayList<>();
		NamedNodeMap nanode = node.getAttributes();
		StatSet generatorConfig = new StatSet();
		int i = nanode.getLength();

		while (i-- > 0)
		{
			Node n = nanode.item(i);
			if (n != null)
			{
				generatorConfig.set(n.getNodeName(), n.getNodeValue());
			}
		}

		this._dateGenerator = new AuctionDateGenerator(generatorConfig);

		for (Node na = node.getFirstChild(); na != null; na = na.getNextSibling())
		{
			try
			{
				if ("item".equalsIgnoreCase(na.getNodeName()))
				{
					NamedNodeMap naa = na.getAttributes();
					int auctionItemId = Integer.parseInt(naa.getNamedItem("auctionItemId").getNodeValue());
					int auctionLength = Integer.parseInt(naa.getNamedItem("auctionLength").getNodeValue());
					long auctionInitBid = Integer.parseInt(naa.getNamedItem("auctionInitBid").getNodeValue());
					int itemId = Integer.parseInt(naa.getNamedItem("itemId").getNodeValue());
					int itemCount = Integer.parseInt(naa.getNamedItem("itemCount").getNodeValue());
					if (auctionLength < 1)
					{
						throw new IllegalArgumentException("auctionLength < 1 for instanceId: " + this._instanceId + ", itemId " + itemId);
					}

					StatSet itemExtra = new StatSet();
					AuctionItem item = new AuctionItem(auctionItemId, auctionLength, auctionInitBid, itemId, itemCount, itemExtra);
					if (!item.checkItemExists())
					{
						throw new IllegalArgumentException("Item with id " + itemId + " not found");
					}

					for (AuctionItem tmp : this._items)
					{
						if (tmp.getAuctionItemId() == auctionItemId)
						{
							throw new IllegalArgumentException("Dublicated auction item id " + auctionItemId);
						}
					}

					this._items.add(item);

					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("extra".equalsIgnoreCase(nb.getNodeName()))
						{
							NamedNodeMap nab = nb.getAttributes();
							int ix = nab.getLength();

							while (ix-- > 0)
							{
								Node n = nab.item(ix);
								if (n != null)
								{
									itemExtra.set(n.getNodeName(), n.getNodeValue());
								}
							}
						}
					}
				}
			}
			catch (IllegalArgumentException var28)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading auction item", var28);
			}
		}

		if (this._items.isEmpty())
		{
			throw new IllegalArgumentException("No items defined");
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT auctionId FROM item_auction WHERE instanceId = ?");)
		{
			ps.setInt(1, this._instanceId);

			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int auctionId = rset.getInt(1);

					try
					{
						ItemAuction auction = this.loadAuction(auctionId);
						if (auction != null)
						{
							this._auctions.put(auctionId, auction);
						}
						else
						{
							ItemAuctionManager.deleteAuction(auctionId);
						}
					}
					catch (SQLException var23)
					{
						LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading auction: " + auctionId, var23);
					}
				}
			}
		}
		catch (SQLException var27)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed loading auctions.", var27);
			return;
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._items.size() + " item(s) and registered " + this._auctions.size() + " auction(s) for instance " + this._instanceId);
		this.checkAndSetCurrentAndNextAuction();
	}

	public ItemAuction getCurrentAuction()
	{
		return this._currentAuction;
	}

	public ItemAuction getNextAuction()
	{
		return this._nextAuction;
	}

	public void shutdown()
	{
		ScheduledFuture<?> stateTask = this._stateTask;
		if (stateTask != null)
		{
			stateTask.cancel(false);
		}
	}

	private AuctionItem getAuctionItem(int auctionItemId)
	{
		int i = this._items.size();

		while (i-- > 0)
		{
			AuctionItem item = this._items.get(i);
			if (item.getAuctionItemId() == auctionItemId)
			{
				return item;
			}
		}

		return null;
	}

	protected void checkAndSetCurrentAndNextAuction()
	{
		ItemAuction[] auctions = this._auctions.values().toArray(new ItemAuction[this._auctions.size()]);
		ItemAuction currentAuction = null;
		ItemAuction nextAuction = null;

		nextAuction = switch (auctions.length)
		{
			case 0 -> this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
			case 1 -> {
				switch (auctions[0].getAuctionState())
				{
					case CREATED:
						if (auctions[0].getStartingTime() < System.currentTimeMillis() + START_TIME_SPACE)
						{
							currentAuction = auctions[0];
							yield this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						}
						yield auctions[0];
					case STARTED:
						currentAuction = auctions[0];
						yield this.createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
					case FINISHED:
						currentAuction = auctions[0];
						yield this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
					default:
						throw new IllegalArgumentException();
				}
			}
			default -> {
				Arrays.sort(auctions, Comparator.comparingLong(ItemAuction::getStartingTime).reversed());
				long currentTime = System.currentTimeMillis();

				for (ItemAuction auction : auctions)
				{
					if ((auction.getAuctionState() == ItemAuctionState.STARTED) || (auction.getStartingTime() <= currentTime))
					{
						currentAuction = auction;
						break;
					}
				}

				for (ItemAuction auction : auctions)
				{
					if (auction.getStartingTime() > currentTime && currentAuction != auction)
					{
						yield auction;
					}
				}

				yield this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
			}
		};

		this._auctions.put(nextAuction.getAuctionId(), nextAuction);
		this._currentAuction = currentAuction;
		this._nextAuction = nextAuction;
		if (currentAuction != null && currentAuction.getAuctionState() != ItemAuctionState.FINISHED)
		{
			if (currentAuction.getAuctionState() == ItemAuctionState.STARTED)
			{
				this.setStateTask(ThreadPool.schedule(new ItemAuctionInstance.ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)));
			}
			else
			{
				this.setStateTask(ThreadPool.schedule(new ItemAuctionInstance.ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Schedule current auction " + currentAuction.getAuctionId() + " for instance " + this._instanceId);
		}
		else
		{
			this.setStateTask(ThreadPool.schedule(new ItemAuctionInstance.ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			LOGGER.info(this.getClass().getSimpleName() + ": Schedule next auction " + nextAuction.getAuctionId() + " on " + this.DATE_FORMAT.format(new Date(nextAuction.getStartingTime())) + " for instance " + this._instanceId);
		}
	}

	public ItemAuction getAuction(int auctionId)
	{
		return this._auctions.get(auctionId);
	}

	public ArrayList<ItemAuction> getAuctionsByBidder(int bidderObjId)
	{
		Collection<ItemAuction> auctions = this.getAuctions();
		ArrayList<ItemAuction> stack = new ArrayList<>(auctions.size());

		for (ItemAuction auction : this.getAuctions())
		{
			if (auction.getAuctionState() != ItemAuctionState.CREATED)
			{
				ItemAuctionBid bid = auction.getBidFor(bidderObjId);
				if (bid != null)
				{
					stack.add(auction);
				}
			}
		}

		return stack;
	}

	public Collection<ItemAuction> getAuctions()
	{
		synchronized (this._auctions)
		{
			return this._auctions.values();
		}
	}

	protected void onAuctionFinished(ItemAuction auction)
	{
		auction.broadcastToAllBiddersInternal(new SystemMessage(SystemMessageId.S1_S_AUCTION_HAS_ENDED).addInt(auction.getAuctionId()));
		ItemAuctionBid bid = auction.getHighestBid();
		if (bid != null)
		{
			Item item = auction.createNewItemInstance();
			Player player = bid.getPlayer();
			if (player != null)
			{
				player.getWarehouse().addItem(ItemProcessType.BUY, item, null, null);
				player.sendPacket(SystemMessageId.YOU_HAVE_BID_THE_HIGHEST_PRICE_AND_HAVE_WON_THE_ITEM_THE_ITEM_CAN_BE_FOUND_IN_YOUR_PERSONAL_WAREHOUSE);
				LOGGER.info(this.getClass().getSimpleName() + ": Auction " + auction.getAuctionId() + " has finished. Highest bid by " + player.getName() + " for instance " + this._instanceId);
			}
			else
			{
				item.setOwnerId(bid.getPlayerObjId());
				item.setItemLocation(ItemLocation.WAREHOUSE);
				item.updateDatabase();
				World.getInstance().removeObject(item);
				LOGGER.info(this.getClass().getSimpleName() + ": Auction " + auction.getAuctionId() + " has finished. Highest bid by " + CharInfoTable.getInstance().getNameById(bid.getPlayerObjId()) + " for instance " + this._instanceId);
			}

			auction.clearCanceledBids();
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Auction " + auction.getAuctionId() + " has finished. There have not been any bid for instance " + this._instanceId);
		}
	}

	protected void setStateTask(ScheduledFuture<?> future)
	{
		ScheduledFuture<?> stateTask = this._stateTask;
		if (stateTask != null)
		{
			stateTask.cancel(false);
		}

		this._stateTask = future;
	}

	private ItemAuction createAuction(long after)
	{
		AuctionItem auctionItem = this._items.get(Rnd.get(this._items.size()));
		long startingTime = this._dateGenerator.nextDate(after);
		long endingTime = startingTime + TimeUnit.MILLISECONDS.convert(auctionItem.getAuctionLength(), TimeUnit.MINUTES);
		ItemAuction auction = new ItemAuction(this._auctionIds.getAndIncrement(), this._instanceId, startingTime, endingTime, auctionItem);
		auction.storeMe();
		return auction;
	}

	private ItemAuction loadAuction(int auctionId) throws SQLException
	{
		ItemAuction var45;
		try (Connection con = DatabaseFactory.getConnection())
		{
			int auctionItemId = 0;
			long startingTime = 0L;
			long endingTime = 0L;
			byte auctionStateId = 0;

			try (PreparedStatement ps = con.prepareStatement("SELECT auctionItemId, startingTime, endingTime, auctionStateId FROM item_auction WHERE auctionId = ? "))
			{
				ps.setInt(1, auctionId);

				try (ResultSet rset = ps.executeQuery())
				{
					if (!rset.next())
					{
						LOGGER.warning(this.getClass().getSimpleName() + ": Auction data not found for auction: " + auctionId);
						return null;
					}

					auctionItemId = rset.getInt(1);
					startingTime = rset.getLong(2);
					endingTime = rset.getLong(3);
					auctionStateId = rset.getByte(4);
				}
			}

			if (startingTime >= endingTime)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Invalid starting/ending paramaters for auction: " + auctionId);
				return null;
			}

			AuctionItem auctionItem = this.getAuctionItem(auctionItemId);
			if (auctionItem == null)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": AuctionItem: " + auctionItemId + ", not found for auction: " + auctionId);
				return null;
			}

			ItemAuctionState auctionState = ItemAuctionState.stateForStateId(auctionStateId);
			if (auctionState == null)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Invalid auctionStateId: " + auctionStateId + ", for auction: " + auctionId);
				return null;
			}

			if (auctionState == ItemAuctionState.FINISHED && startingTime < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(GeneralConfig.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Clearing expired auction: " + auctionId);

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_auction WHERE auctionId = ?"))
				{
					ps.setInt(1, auctionId);
					ps.execute();
				}

				try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId = ?"))
				{
					ps.setInt(1, auctionId);
					ps.execute();
				}

				return null;
			}

			List<ItemAuctionBid> auctionBids = new ArrayList<>();

			try (PreparedStatement ps = con.prepareStatement("SELECT playerObjId, playerBid FROM item_auction_bid WHERE auctionId = ?"))
			{
				ps.setInt(1, auctionId);

				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						int playerObjId = rs.getInt(1);
						long playerBid = rs.getLong(2);
						ItemAuctionBid bid = new ItemAuctionBid(playerObjId, playerBid);
						auctionBids.add(bid);
					}
				}
			}

			var45 = new ItemAuction(auctionId, this._instanceId, startingTime, endingTime, auctionItem, auctionBids, auctionState);
		}

		return var45;
	}

	private class ScheduleAuctionTask implements Runnable
	{
		private final ItemAuction _auction;

		public ScheduleAuctionTask(ItemAuction auction)
		{
			Objects.requireNonNull(ItemAuctionInstance.this);
			super();
			this._auction = auction;
		}

		@Override
		public void run()
		{
			try
			{
				this.runImpl();
			}
			catch (Exception var2)
			{
				ItemAuctionInstance.LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed scheduling auction " + this._auction.getAuctionId(), var2);
			}
		}

		private void runImpl() throws Exception
		{
			ItemAuctionState state = this._auction.getAuctionState();
			switch (state)
			{
				case CREATED:
					if (!this._auction.setAuctionState(state, ItemAuctionState.STARTED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED + ", expected: " + state);
					}

					ItemAuctionInstance.LOGGER.info(this.getClass().getSimpleName() + ": Auction " + this._auction.getAuctionId() + " has started for instance " + this._auction.getInstanceId());
					ItemAuctionInstance.this.checkAndSetCurrentAndNextAuction();
					break;
				case STARTED:
					switch (this._auction.getAuctionEndingExtendState())
					{
						case EXTEND_BY_5_MIN:
							if (this._auction.getScheduledAuctionEndingExtendState() == ItemAuctionExtendState.INITIAL)
							{
								this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_5_MIN);
								ItemAuctionInstance.this.setStateTask(ThreadPool.schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						case EXTEND_BY_3_MIN:
							if (this._auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_3_MIN)
							{
								this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_3_MIN);
								ItemAuctionInstance.this.setStateTask(ThreadPool.schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						case EXTEND_BY_CONFIG_PHASE_A:
							if (this._auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
							{
								this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B);
								ItemAuctionInstance.this.setStateTask(ThreadPool.schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						case EXTEND_BY_CONFIG_PHASE_B:
							if (this._auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
							{
								this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A);
								ItemAuctionInstance.this.setStateTask(ThreadPool.schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
					}

					if (!this._auction.setAuctionState(state, ItemAuctionState.FINISHED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED + ", expected: " + state);
					}

					ItemAuctionInstance.this.onAuctionFinished(this._auction);
					ItemAuctionInstance.this.checkAndSetCurrentAndNextAuction();
					break;
				default:
					throw new IllegalStateException("Invalid state: " + state);
			}
		}
	}
}
