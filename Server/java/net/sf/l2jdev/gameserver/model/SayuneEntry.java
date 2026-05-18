package net.sf.l2jdev.gameserver.model;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.interfaces.ILocational;

public class SayuneEntry implements ILocational
{
	private boolean _isSelector = false;
	private final int _id;
	private int _x;
	private int _y;
	private int _z;
	private final List<SayuneEntry> _innerEntries = new LinkedList<>();

	public SayuneEntry(int id)
	{
		this._id = id;
	}

	public SayuneEntry(boolean isSelector, int id, int x, int y, int z)
	{
		this._isSelector = isSelector;
		this._id = id;
		this._x = x;
		this._y = y;
		this._z = z;
	}

	public int getId()
	{
		return this._id;
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
	public int getHeading()
	{
		return 0;
	}

	@Override
	public ILocational getLocation()
	{
		return new Location(this._x, this._y, this._z);
	}

	public boolean isSelector()
	{
		return this._isSelector;
	}

	public List<SayuneEntry> getInnerEntries()
	{
		return this._innerEntries;
	}

	public SayuneEntry addInnerEntry(SayuneEntry innerEntry)
	{
		this._innerEntries.add(innerEntry);
		return innerEntry;
	}
}
