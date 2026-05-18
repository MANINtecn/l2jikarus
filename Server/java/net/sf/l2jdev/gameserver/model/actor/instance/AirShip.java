package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.ai.AirShipAI;
import net.sf.l2jdev.gameserver.managers.AirShipManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Vehicle;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAirShipInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExGetOffAirShip;
import net.sf.l2jdev.gameserver.network.serverpackets.ExGetOnAirShip;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import net.sf.l2jdev.gameserver.network.serverpackets.ExStopMoveAirShip;

public class AirShip extends Vehicle
{
	public AirShip(CreatureTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.AirShip);
		this.setAI(new AirShipAI(this));
	}

	@Override
	public boolean isAirShip()
	{
		return true;
	}

	public boolean isOwner(Player player)
	{
		return false;
	}

	public int getOwnerId()
	{
		return 0;
	}

	public boolean isCaptain(Player player)
	{
		return false;
	}

	public int getCaptainId()
	{
		return 0;
	}

	public int getHelmObjectId()
	{
		return 0;
	}

	public int getHelmItemId()
	{
		return 0;
	}

	public boolean setCaptain(Player player)
	{
		return false;
	}

	public int getFuel()
	{
		return 0;
	}

	public void setFuel(int f)
	{
	}

	public int getMaxFuel()
	{
		return 0;
	}

	public void setMaxFuel(int mf)
	{
	}

	@Override
	public int getId()
	{
		return 0;
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		boolean result = super.moveToNextRoutePoint();
		if (result)
		{
			this.broadcastPacket(new ExMoveToLocationAirShip(this));
		}

		return result;
	}

	@Override
	public boolean addPassenger(Player player)
	{
		if (!super.addPassenger(player))
		{
			return false;
		}
		player.setVehicle(this);
		player.setInVehiclePosition(new Location(0, 0, 0));
		player.broadcastPacket(new ExGetOnAirShip(player, this));
		player.setXYZ(this.getX(), this.getY(), this.getZ());
		player.revalidateZone(true);
		player.stopMove(null);
		return true;
	}

	@Override
	public void oustPlayer(Player player)
	{
		super.oustPlayer(player);
		Location loc = this.getOustLoc();
		if (player.isOnline())
		{
			player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
		}
		else
		{
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
		}
	}

	@Override
	public boolean deleteMe()
	{
		if (!super.deleteMe())
		{
			return false;
		}
		AirShipManager.getInstance().removeAirShip(this);
		return true;
	}

	@Override
	public void stopMove(Location loc)
	{
		super.stopMove(loc);
		this.broadcastPacket(new ExStopMoveAirShip(this));
	}

	@Override
	public void updateAbnormalVisualEffects()
	{
		this.broadcastPacket(new ExAirShipInfo(this));
	}

	@Override
	public void sendInfo(Player player)
	{
		if (this.isVisibleFor(player))
		{
			player.sendPacket(new ExAirShipInfo(this));
		}
	}
}
