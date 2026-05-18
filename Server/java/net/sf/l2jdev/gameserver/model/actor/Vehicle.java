package net.sf.l2jdev.gameserver.model.actor;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.VehiclePathPoint;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.stat.VehicleStat;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.zone.ZoneRegion;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.MovementTaskManager;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public abstract class Vehicle extends Creature
{
	protected int _dockId = 0;
	protected final Set<Player> _passengers = ConcurrentHashMap.newKeySet();
	protected Location _oustLoc = null;
	private Runnable _engine = null;
	protected VehiclePathPoint[] _currentPath = null;
	protected int _runState = 0;
	private ScheduledFuture<?> _monitorTask = null;
	private final Location _monitorLocation = new Location(this);

	public Vehicle(CreatureTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Vehicle);
		this.setFlying(true);
	}

	public boolean isBoat()
	{
		return false;
	}

	public boolean isAirShip()
	{
		return false;
	}

	public boolean canBeControlled()
	{
		return this._engine == null;
	}

	public void registerEngine(Runnable r)
	{
		this._engine = r;
	}

	public void runEngine(int delay)
	{
		if (this._engine != null)
		{
			ThreadPool.schedule(this._engine, delay);
		}
	}

	public void executePath(VehiclePathPoint[] path)
	{
		this._runState = 0;
		this._currentPath = path;
		if (this._currentPath != null && this._currentPath.length > 0)
		{
			VehiclePathPoint point = this._currentPath[0];
			if (point.getMoveSpeed() > 0)
			{
				this.getStat().setMoveSpeed(point.getMoveSpeed());
			}

			if (point.getRotationSpeed() > 0)
			{
				this.getStat().setRotationSpeed(point.getRotationSpeed());
			}

			this.getAI().setIntention(Intention.MOVE_TO, new Location(point.getX(), point.getY(), point.getZ(), 0));
		}
		else
		{
			this.getAI().setIntention(Intention.ACTIVE);
		}
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		this._move = null;
		if (this._currentPath != null)
		{
			this._runState++;
			if (this._runState < this._currentPath.length)
			{
				VehiclePathPoint point = this._currentPath[this._runState];
				if (!this.isMovementDisabled())
				{
					if (point.getMoveSpeed() != 0)
					{
						if (point.getMoveSpeed() > 0)
						{
							this.getStat().setMoveSpeed(point.getMoveSpeed());
						}

						if (point.getRotationSpeed() > 0)
						{
							this.getStat().setRotationSpeed(point.getRotationSpeed());
						}

						Creature.MoveData m = new Creature.MoveData();
						m.disregardingGeodata = false;
						m.onGeodataPathIndex = -1;
						m.xDestination = point.getX();
						m.yDestination = point.getY();
						m.zDestination = point.getZ();
						m.heading = 0;
						double distance = Math.hypot(point.getX() - this.getX(), point.getY() - this.getY());
						if (distance > 1.0)
						{
							this.setHeading(LocationUtil.calculateHeadingFrom(this.getX(), this.getY(), point.getX(), point.getY()));
						}

						m.moveStartTime = GameTimeTaskManager.getInstance().getGameTicks();
						this._move = m;
						MovementTaskManager.getInstance().registerMovingObject(this);
						if (this._monitorTask == null)
						{
							this._monitorTask = ThreadPool.scheduleAtFixedRate(() -> {
								if (this.isInDock() || this.calculateDistance3D(this._monitorLocation) != 0.0)
								{
									this._monitorLocation.setXYZ(this);
								}
								else if (this._currentPath != null)
								{
									if (this._runState < this._currentPath.length)
									{
										this._runState = Math.max(0, this._runState - 1);
										this.moveToNextRoutePoint();
									}
									else
									{
										this.broadcastInfo();
									}
								}
							}, 1000L, 1000L);
						}

						return true;
					}

					point.setHeading(point.getRotationSpeed());
					this.teleToLocation(point, false);
					if (this._monitorTask != null)
					{
						this._monitorTask.cancel(true);
						this._monitorTask = null;
					}

					this._currentPath = null;
				}
			}
			else
			{
				if (this._monitorTask != null)
				{
					this._monitorTask.cancel(true);
					this._monitorTask = null;
				}

				this._currentPath = null;
			}
		}

		this.runEngine(10);
		return false;
	}

	@Override
	public VehicleStat getStat()
	{
		return (VehicleStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new VehicleStat(this));
	}

	public boolean isInDock()
	{
		return this._dockId > 0;
	}

	public int getDockId()
	{
		return this._dockId;
	}

	public void setInDock(int d)
	{
		this._dockId = d;
	}

	public void setOustLoc(Location loc)
	{
		this._oustLoc = loc;
	}

	public Location getOustLoc()
	{
		return this._oustLoc != null ? this._oustLoc : MapRegionManager.getInstance().getTeleToLocation(this, TeleportWhereType.TOWN);
	}

	public void oustPlayers()
	{
		Iterator<Player> iter = this._passengers.iterator();

		while (iter.hasNext())
		{
			Player player = iter.next();
			iter.remove();
			if (player != null)
			{
				this.oustPlayer(player);
			}
		}
	}

	public void oustPlayer(Player player)
	{
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		this.removePassenger(player);
	}

	public boolean addPassenger(Player player)
	{
		if (player != null && !this._passengers.contains(player))
		{
			if (player.getVehicle() != null && player.getVehicle() != this)
			{
				return false;
			}
			this._passengers.add(player);
			return true;
		}
		return false;
	}

	public void removePassenger(Player player)
	{
		try
		{
			this._passengers.remove(player);
		}
		catch (Exception var3)
		{
		}
	}

	public boolean isEmpty()
	{
		return this._passengers.isEmpty();
	}

	public Set<Player> getPassengers()
	{
		return this._passengers;
	}

	public void broadcastToPassengers(ServerPacket packet)
	{
		for (Player player : this._passengers)
		{
			if (player != null)
			{
				player.sendPacket(packet);
			}
		}
	}

	public void payForRide(int itemId, int count, int oustX, int oustY, int oustZ)
	{
		World.getInstance().forEachVisibleObjectInRange(this, Player.class, 1000, player -> {
			if (player.isInBoat() && player.getBoat() == this)
			{
				if (itemId > 0)
				{
					Item ticket = player.getInventory().getItemByItemId(itemId);
					if (ticket == null || player.getInventory().destroyItem(ItemProcessType.FEE, ticket, count, player, this) == null)
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_POSSESS_THE_CORRECT_TICKET_TO_BOARD_THE_BOAT);
						player.teleToLocation(new Location(oustX, oustY, oustZ), true);
						return;
					}

					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(ticket);
					player.sendInventoryUpdate(iu);
				}

				this.addPassenger(player);
			}
		});
	}

	@Override
	public boolean updatePosition()
	{
		boolean result = super.updatePosition();

		for (Player player : this._passengers)
		{
			if (player != null && player.getVehicle() == this)
			{
				player.setXYZ(this.getX(), this.getY(), this.getZ());
				player.revalidateZone(false);
			}
		}

		return result;
	}

	@Override
	public void teleToLocation(ILocational loc, boolean allowRandomOffset)
	{
		if (this.isMoving())
		{
			this.stopMove(null);
		}

		this.setTeleporting(true);
		this.getAI().setIntention(Intention.ACTIVE);

		for (Player player : this._passengers)
		{
			if (player != null)
			{
				player.teleToLocation(loc, false);
			}
		}

		this.decayMe();
		this.setXYZ(loc);
		if (loc.getHeading() != 0)
		{
			this.setHeading(loc.getHeading());
		}

		this.onTeleported();
		this.revalidateZone(true);
	}

	@Override
	public void stopMove(Location loc)
	{
		this._move = null;
		if (loc != null)
		{
			this.setXYZ(loc);
			this.setHeading(loc.getHeading());
			this.revalidateZone(true);
		}
	}

	@Override
	public boolean deleteMe()
	{
		this._engine = null;

		try
		{
			if (this.isMoving())
			{
				this.stopMove(null);
			}
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.SEVERE, "Failed stopMove().", var5);
		}

		try
		{
			this.oustPlayers();
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.SEVERE, "Failed oustPlayers().", var4);
		}

		ZoneRegion oldZoneRegion = ZoneManager.getInstance().getRegion(this);

		try
		{
			this.decayMe();
		}
		catch (Exception var3)
		{
			LOGGER.log(Level.SEVERE, "Failed decayMe().", var3);
		}

		oldZoneRegion.removeFromZones(this);
		return super.deleteMe();
	}

	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public void detachAI()
	{
	}

	@Override
	public boolean isVehicle()
	{
		return true;
	}
}
