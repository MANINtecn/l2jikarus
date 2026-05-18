package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;

import net.sf.l2jdev.gameserver.ai.ShuttleAI;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Vehicle;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.model.shuttle.ShuttleDataHolder;
import net.sf.l2jdev.gameserver.model.shuttle.ShuttleStop;
import net.sf.l2jdev.gameserver.network.serverpackets.shuttle.ExShuttleGetOff;
import net.sf.l2jdev.gameserver.network.serverpackets.shuttle.ExShuttleGetOn;
import net.sf.l2jdev.gameserver.network.serverpackets.shuttle.ExShuttleInfo;

public class Shuttle extends Vehicle
{
	private ShuttleDataHolder _shuttleData;

	public Shuttle(CreatureTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Shuttle);
		this.setAI(new ShuttleAI(this));
	}

	public List<ShuttleStop> getStops()
	{
		return this._shuttleData.getStops();
	}

	public void closeDoor(int id)
	{
		for (ShuttleStop stop : this._shuttleData.getStops())
		{
			if (stop.getId() == id)
			{
				stop.closeDoor();
				break;
			}
		}
	}

	public void openDoor(int id)
	{
		for (ShuttleStop stop : this._shuttleData.getStops())
		{
			if (stop.getId() == id)
			{
				stop.openDoor();
				break;
			}
		}
	}

	@Override
	public int getId()
	{
		return this._shuttleData.getId();
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
		player.broadcastPacket(new ExShuttleGetOn(player, this));
		player.setXYZ(this.getX(), this.getY(), this.getZ());
		player.revalidateZone(true);
		return true;
	}

	public void removePassenger(Player player, int x, int y, int z)
	{
		this.oustPlayer(player);
		if (player.isOnline())
		{
			player.broadcastPacket(new ExShuttleGetOff(player, this, x, y, z));
			player.setXYZ(x, y, z);
			player.revalidateZone(true);
		}
		else
		{
			player.setXYZInvisible(x, y, z);
		}
	}

	@Override
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

	@Override
	public void sendInfo(Player player)
	{
		player.sendPacket(new ExShuttleInfo(this));
	}

	public void broadcastShuttleInfo()
	{
		this.broadcastPacket(new ExShuttleInfo(this));
	}

	public void setData(ShuttleDataHolder data)
	{
		this._shuttleData = data;
	}

	public ShuttleDataHolder getShuttleData()
	{
		return this._shuttleData;
	}
}
