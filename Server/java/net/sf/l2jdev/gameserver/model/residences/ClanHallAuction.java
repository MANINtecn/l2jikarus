package net.sf.l2jdev.gameserver.model.residences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;

public class ClanHallAuction
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallAuction.class.getName());
	private final int _clanHallId;
	public static final String LOAD_CLANHALL_BIDDERS = "SELECT * FROM clanhall_auctions_bidders WHERE clanHallId=?";
	public static final String DELETE_CLANHALL_BIDDERS = "DELETE FROM clanhall_auctions_bidders WHERE clanHallId=?";
	public static final String INSERT_CLANHALL_BIDDER = "REPLACE INTO clanhall_auctions_bidders (clanHallId, clanId, bid, bidTime) VALUES (?,?,?,?)";
	public static final String DELETE_CLANHALL_BIDDER = "DELETE FROM clanhall_auctions_bidders WHERE clanId=?";
	private Map<Integer, Bidder> _bidders;

	public ClanHallAuction(int clanHallId)
	{
		this._clanHallId = clanHallId;
		this.loadBidder();
	}

	private void loadBidder()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM clanhall_auctions_bidders WHERE clanHallId=?");)
		{
			ps.setInt(1, this._clanHallId);

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					Clan clan = ClanTable.getInstance().getClan(rs.getInt("clanId"));
					this.addBid(clan, rs.getLong("bid"), rs.getLong("bidTime"));
				}
			}
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.WARNING, "Failed loading clan hall auctions bidder for clan hall " + this._clanHallId + ".", var12);
		}
	}

	public Map<Integer, Bidder> getBids()
	{
		return this._bidders == null ? Collections.emptyMap() : this._bidders;
	}

	public void addBid(Clan clan, long bid)
	{
		this.addBid(clan, bid, System.currentTimeMillis());
	}

	public void addBid(Clan clan, long bid, long bidTime)
	{
		if (this._bidders == null)
		{
			synchronized (this)
			{
				if (this._bidders == null)
				{
					this._bidders = new ConcurrentHashMap<>();
				}
			}
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO clanhall_auctions_bidders (clanHallId, clanId, bid, bidTime) VALUES (?,?,?,?)");)
		{
			ps.setInt(1, this._clanHallId);
			ps.setInt(2, clan.getId());
			ps.setLong(3, bid);
			ps.setLong(4, bidTime);
			ps.execute();
			this._bidders.put(clan.getId(), new Bidder(clan, bid, bidTime));
		}
		catch (SQLException var15)
		{
			LOGGER.log(Level.WARNING, "Failed insert clan hall auctions bidder " + clan.getName() + " for clan hall " + this._clanHallId + ".", var15);
		}
	}

	public void removeBid(Clan clan)
	{
		this.getBids().remove(clan.getId());

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_auctions_bidders WHERE clanId=?");)
		{
			ps.setInt(1, clan.getId());
			ps.execute();
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.SEVERE, "Failed clearing bidder " + clan.getName() + " for clan hall " + this._clanHallId + ": ", var10);
		}
	}

	public long getHighestBid()
	{
		ClanHall clanHall = ClanHallData.getInstance().getClanHallById(this._clanHallId);
		return this.getBids().values().stream().mapToLong(Bidder::getBid).max().orElse(clanHall.getMinBid());
	}

	public long getClanBid(Clan clan)
	{
		return this.getBids().get(clan.getId()).getBid();
	}

	public Optional<Bidder> getHighestBidder()
	{
		return this.getBids().values().stream().sorted(Comparator.comparingLong(Bidder::getBid).reversed()).findFirst();
	}

	public int getBidCount()
	{
		return this.getBids().values().size();
	}

	public void returnAdenas(Bidder bidder)
	{
		bidder.getClan().getWarehouse().addItem(ItemProcessType.REFUND, 57, bidder.getBid(), null, null);
	}

	public void finalizeAuctions()
	{
		Optional<Bidder> potentialHighestBidder = this.getHighestBidder();
		if (potentialHighestBidder.isPresent())
		{
			Bidder highestBidder = potentialHighestBidder.get();
			ClanHall clanHall = ClanHallData.getInstance().getClanHallById(this._clanHallId);
			clanHall.setOwner(highestBidder.getClan());
			this.getBids().clear();

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_auctions_bidders WHERE clanHallId=?");)
			{
				ps.setInt(1, this._clanHallId);
				ps.execute();
			}
			catch (Exception var12)
			{
				LOGGER.log(Level.SEVERE, "Failed clearing bidder for clan hall " + this._clanHallId + ": ", var12);
			}
		}
	}

	public int getClanHallId()
	{
		return this._clanHallId;
	}
}
