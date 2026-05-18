package net.sf.l2jdev.gameserver.model.shuttle;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.Location;

public class ShuttleStop
{
	private final int _id;
	private boolean _isOpen = true;
	private final List<Location> _dimensions = new ArrayList<>(3);
	private long _lastDoorStatusChanges = System.currentTimeMillis();

	public ShuttleStop(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}

	public boolean isDoorOpen()
	{
		return this._isOpen;
	}

	public void addDimension(Location loc)
	{
		this._dimensions.add(loc);
	}

	public List<Location> getDimensions()
	{
		return this._dimensions;
	}

	public void openDoor()
	{
		if (!this._isOpen)
		{
			this._isOpen = true;
			this._lastDoorStatusChanges = System.currentTimeMillis();
		}
	}

	public void closeDoor()
	{
		if (this._isOpen)
		{
			this._isOpen = false;
			this._lastDoorStatusChanges = System.currentTimeMillis();
		}
	}

	public boolean hasDoorChanged()
	{
		return System.currentTimeMillis() - this._lastDoorStatusChanges <= 1000L;
	}
}
