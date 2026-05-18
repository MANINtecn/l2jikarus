package net.sf.l2jdev.gameserver.model.events.returns;

import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;

public class LocationReturn extends TerminateReturn
{
	private final boolean _overrideLocation;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private Instance _instance;

	public LocationReturn(boolean terminate, boolean overrideLocation)
	{
		super(terminate, false, false);
		this._overrideLocation = overrideLocation;
	}

	public LocationReturn(boolean terminate, boolean overrideLocation, ILocational targetLocation, Instance instance)
	{
		super(terminate, false, false);
		this._overrideLocation = overrideLocation;
		if (targetLocation != null)
		{
			this.setX(targetLocation.getX());
			this.setY(targetLocation.getY());
			this.setZ(targetLocation.getZ());
			this.setHeading(targetLocation.getHeading());
			this.setInstance(instance);
		}
	}

	public void setX(int x)
	{
		this._x = x;
	}

	public void setY(int y)
	{
		this._y = y;
	}

	public void setZ(int z)
	{
		this._z = z;
	}

	public void setHeading(int heading)
	{
		this._heading = heading;
	}

	public void setInstance(Instance instance)
	{
		this._instance = instance;
	}

	public boolean overrideLocation()
	{
		return this._overrideLocation;
	}

	public int getX()
	{
		return this._x;
	}

	public int getY()
	{
		return this._y;
	}

	public int getZ()
	{
		return this._z;
	}

	public int getHeading()
	{
		return this._heading;
	}

	public Instance getInstance()
	{
		return this._instance;
	}
}
