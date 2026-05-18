package net.sf.l2jdev.gameserver.model.residences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanHallGrade;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanHallType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ClanHall extends AbstractResidence
{
	private static final Logger LOGGER = Logger.getLogger(ClanHall.class.getName());
	private final ClanHallType _type;
	private final int _minBid;
	final int _lease;
	private final int _deposit;
	private final Set<Integer> _npcs = new HashSet<>();
	private final Set<Door> _doors = new HashSet<>();
	private final Set<ClanHallTeleportHolder> _teleports = new HashSet<>();
	private final Location _ownerLocation;
	private final Location _banishLocation;
	Clan _owner = null;
	long _paidUntil = 0L;
	protected ScheduledFuture<?> _checkPaymentTask = null;
	public static final String INSERT_CLANHALL = "INSERT INTO clanhall (id, ownerId, paidUntil) VALUES (?,?,?)";
	public static final String LOAD_CLANHALL = "SELECT * FROM clanhall WHERE id=?";
	public static final String UPDATE_CLANHALL = "UPDATE clanhall SET ownerId=?,paidUntil=? WHERE id=?";

	public ClanHall(StatSet params)
	{
		super(params.getInt("id"));
		this.setName(params.getString("name"));
		this._grade = params.getEnum("grade", ClanHallGrade.class);
		this._type = params.getEnum("type", ClanHallType.class);
		this._minBid = params.getInt("minBid");
		this._lease = params.getInt("lease");
		this._deposit = params.getInt("deposit");
		List<Integer> npcs = params.getList("npcList", Integer.class);
		if (npcs != null)
		{
			this._npcs.addAll(npcs);
		}

		List<Door> doors = params.getList("doorList", Door.class);
		if (doors != null)
		{
			this._doors.addAll(doors);
		}

		List<ClanHallTeleportHolder> teleports = params.getList("teleportList", ClanHallTeleportHolder.class);
		if (teleports != null)
		{
			this._teleports.addAll(teleports);
		}

		this._ownerLocation = params.getLocation("owner_loc");
		this._banishLocation = params.getLocation("banish_loc");
		this.load();
		this.initResidenceZone();
		this.initFunctions();
	}

	@Override
	protected void load()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement loadStatement = con.prepareStatement("SELECT * FROM clanhall WHERE id=?"); PreparedStatement insertStatement = con.prepareStatement("INSERT INTO clanhall (id, ownerId, paidUntil) VALUES (?,?,?)");)
		{
			loadStatement.setInt(1, this.getResidenceId());

			try (ResultSet rset = loadStatement.executeQuery())
			{
				if (rset.next())
				{
					this.setPaidUntil(rset.getLong("paidUntil"));
					this.setOwner(rset.getInt("ownerId"));
				}
				else
				{
					insertStatement.setInt(1, this.getResidenceId());
					insertStatement.setInt(2, 0);
					insertStatement.setInt(3, 0);
					if (insertStatement.execute())
					{
						LOGGER.info("Clan Hall " + this.getName() + " (" + this.getResidenceId() + ") was sucessfully created.");
					}
				}
			}
		}
		catch (SQLException var15)
		{
			LOGGER.log(Level.INFO, "Failed loading clan hall", var15);
		}
	}

	public void updateDB()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE clanhall SET ownerId=?,paidUntil=? WHERE id=?");)
		{
			statement.setInt(1, this.getOwnerId());
			statement.setLong(2, this._paidUntil);
			statement.setInt(3, this.getResidenceId());
			statement.execute();
		}
		catch (SQLException var9)
		{
			LOGGER.warning(var9.toString());
		}
	}

	@Override
	protected void initResidenceZone()
	{
		for (ClanHallZone zone : ZoneManager.getInstance().getAllZones(ClanHallZone.class))
		{
			if (zone.getResidenceId() == this.getResidenceId())
			{
				this.setResidenceZone(zone);
				break;
			}
		}
	}

	public int getCostFailDay()
	{
		Duration failDay = Duration.between(Instant.ofEpochMilli(this._paidUntil), Instant.now());
		return failDay.isNegative() ? 0 : (int) failDay.toDays();
	}

	public void banishOthers()
	{
		this.getResidenceZone().banishForeigners(this.getOwnerId());
	}

	public void openCloseDoors(boolean open)
	{
		this._doors.forEach(door -> door.openCloseMe(open));
	}

	public Set<Door> getDoors()
	{
		return this._doors;
	}

	public Set<Integer> getNpcs()
	{
		return this._npcs;
	}

	public ClanHallType getType()
	{
		return this._type;
	}

	public Clan getOwner()
	{
		return this._owner;
	}

	@Override
	public int getOwnerId()
	{
		Clan owner = this._owner;
		return owner != null ? owner.getId() : 0;
	}

	public void setOwner(int clanId)
	{
		this.setOwner(ClanTable.getInstance().getClan(clanId));
	}

	public void setOwner(Clan clan)
	{
		if (clan != null)
		{
			this._owner = clan;
			clan.setHideoutId(this.getResidenceId());
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			if (this._paidUntil == 0L)
			{
				this.setPaidUntil(Instant.now().plus(Duration.ofDays(7L)).toEpochMilli());
			}

			int failDays = this.getCostFailDay();
			long time = failDays > 0 ? (failDays > 8 ? Instant.now().toEpochMilli() : Instant.ofEpochMilli(this._paidUntil).plus(Duration.ofDays(failDays + 1)).toEpochMilli()) : this._paidUntil;
			this._checkPaymentTask = ThreadPool.schedule(new ClanHall.CheckPaymentTask(), Math.max(0L, time - System.currentTimeMillis()));
		}
		else
		{
			if (this._owner != null)
			{
				this._owner.setHideoutId(0);
				this._owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this._owner));
				this.removeFunctions();
			}

			this._owner = null;
			this.setPaidUntil(0L);
			if (this._checkPaymentTask != null)
			{
				this._checkPaymentTask.cancel(true);
				this._checkPaymentTask = null;
			}
		}

		this.updateDB();
	}

	public long getPaidUntil()
	{
		return this._paidUntil;
	}

	public void setPaidUntil(long paidUntil)
	{
		this._paidUntil = paidUntil;
	}

	public long getNextPayment()
	{
		return this._checkPaymentTask != null ? System.currentTimeMillis() + this._checkPaymentTask.getDelay(TimeUnit.MILLISECONDS) : 0L;
	}

	public Location getOwnerLocation()
	{
		return this._ownerLocation;
	}

	public Location getBanishLocation()
	{
		return this._banishLocation;
	}

	public Set<ClanHallTeleportHolder> getTeleportList()
	{
		return this._teleports;
	}

	public List<ClanHallTeleportHolder> getTeleportList(int functionLevel)
	{
		List<ClanHallTeleportHolder> result = new ArrayList<>();

		for (ClanHallTeleportHolder holder : this._teleports)
		{
			if (holder.getMinFunctionLevel() <= functionLevel)
			{
				result.add(holder);
			}
		}

		return result;
	}

	public int getMinBid()
	{
		return this._minBid;
	}

	public int getLease()
	{
		return this._lease;
	}

	public int getDeposit()
	{
		return this._deposit;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + ":" + this.getName() + "[" + this.getResidenceId() + "]";
	}

	class CheckPaymentTask implements Runnable
	{
		CheckPaymentTask()
		{
			Objects.requireNonNull(ClanHall.this);
			super();
		}

		@Override
		public void run()
		{
			if (ClanHall.this._owner != null)
			{
				if (ClanHall.this._owner.getWarehouse().getAdena() < ClanHall.this._lease)
				{
					if (ClanHall.this.getCostFailDay() > 8)
					{
						ClanHall.this._owner.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						ClanHall.this.setOwner(null);
					}
					else
					{
						ClanHall.this._checkPaymentTask = ThreadPool.schedule(ClanHall.this.new CheckPaymentTask(), 86400000L);
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_DEPOSIT_THE_NECESSARY_AMOUNT_OF_ADENA_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addInt(ClanHall.this._lease);
						ClanHall.this._owner.broadcastToOnlineMembers(sm);
					}
				}
				else
				{
					ClanHall.this._owner.getWarehouse().destroyItem(ItemProcessType.FEE, 57, ClanHall.this._lease, null, null);
					ClanHall.this.setPaidUntil(Instant.ofEpochMilli(ClanHall.this._paidUntil).plus(Duration.ofDays(7L)).toEpochMilli());
					ClanHall.this._checkPaymentTask = ThreadPool.schedule(ClanHall.this.new CheckPaymentTask(), ClanHall.this._paidUntil - System.currentTimeMillis());
					ClanHall.this.updateDB();
				}
			}
		}
	}
}
