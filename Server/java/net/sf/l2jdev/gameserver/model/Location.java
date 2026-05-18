package net.sf.l2jdev.gameserver.model;

import java.util.Objects;

import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.interfaces.IPositionable;

public class Location implements IPositionable
{
	protected volatile int _x;
	protected volatile int _y;
	protected volatile int _z;
	protected volatile int _heading;

	public Location(int x, int y, int z)
	{
		this._x = x;
		this._y = y;
		this._z = z;
		this._heading = 0;
	}

	public Location(int x, int y, int z, int heading)
	{
		this._x = x;
		this._y = y;
		this._z = z;
		this._heading = heading;
	}

	public Location(WorldObject obj)
	{
		this(obj.getX(), obj.getY(), obj.getZ(), obj.getHeading());
	}

	public Location(StatSet set)
	{
		this._x = set.getInt("x", 0);
		this._y = set.getInt("y", 0);
		this._z = set.getInt("z", 0);
		this._heading = set.getInt("heading", 0);
	}

	@Override
	public int getX()
	{
		return this._x;
	}

	@Override
	public int getY()
	{
		return this._y;
	}

	@Override
	public int getZ()
	{
		return this._z;
	}

	@Override
	public void setXYZ(int x, int y, int z)
	{
		this._x = x;
		this._y = y;
		this._z = z;
	}

	@Override
	public void setXYZ(ILocational loc)
	{
		this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public int getHeading()
	{
		return this._heading;
	}

	@Override
	public void setHeading(int heading)
	{
		this._heading = Math.clamp(heading, 0, 65535);
	}

	@Override
	public IPositionable getLocation()
	{
		return this;
	}

	@Override
	public void setLocation(Location loc)
	{
		this._x = loc.getX();
		this._y = loc.getY();
		this._z = loc.getZ();
		this._heading = loc.getHeading();
	}

	@Override
	public Location clone()
	{
		return new Location(this._x, this._y, this._z, this._heading);
	}

	@Override
	public int hashCode()
	{
		return 31 * Objects.hash(this._x, this._y) + Objects.hash(this._z);
	}

	@Override
	public boolean equals(Object obj)
	{
		return !(obj instanceof Location loc) ? false : this.getX() == loc.getX() && this.getY() == loc.getY() && this.getZ() == loc.getZ() && this.getHeading() == loc.getHeading();
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + "] X: " + this._x + " Y: " + this._y + " Z: " + this._z + " Heading: " + this._heading;
	}
}
