package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.Location;

public class SharedTeleportHolder
{
	private final int _id;
	private final String _name;
	private int _count;
	private final Location _location;

	public SharedTeleportHolder(int id, String name, int count, int x, int y, int z)
	{
		this._id = id;
		this._name = name;
		this._count = count;
		this._location = new Location(x, y, z);
	}

	public int getId()
	{
		return this._id;
	}

	public String getName()
	{
		return this._name;
	}

	public int getCount()
	{
		return Math.max(0, this._count);
	}

	public void decrementCount()
	{
		this._count--;
	}

	public Location getLocation()
	{
		return this._location;
	}
}
