package net.sf.l2jdev.gameserver.data.holders;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.Location;

public class TeleportListHolder
{
	private final int _locId;
	private final List<Location> _locations;
	private final int _price;
	private final boolean _special;

	public TeleportListHolder(int locId, int x, int y, int z, int price, boolean special)
	{
		this._locId = locId;
		this._locations = new ArrayList<>(1);
		this._locations.add(new Location(x, y, z));
		this._price = price;
		this._special = special;
	}

	public TeleportListHolder(int locId, List<Location> locations, int price, boolean special)
	{
		this._locId = locId;
		this._locations = locations;
		this._price = price;
		this._special = special;
	}

	public int getLocId()
	{
		return this._locId;
	}

	public List<Location> getLocations()
	{
		return this._locations;
	}

	public int getPrice()
	{
		return this._price;
	}

	public boolean isSpecial()
	{
		return this._special;
	}

	public Location getLocation()
	{
		return this._locations.get(Rnd.get(this._locations.size()));
	}
}
