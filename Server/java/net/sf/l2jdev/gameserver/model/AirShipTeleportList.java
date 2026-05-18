package net.sf.l2jdev.gameserver.model;

public class AirShipTeleportList
{
	private final int _location;
	private final int[] _fuel;
	private final VehiclePathPoint[][] _routes;

	public AirShipTeleportList(int loc, int[] f, VehiclePathPoint[][] r)
	{
		this._location = loc;
		this._fuel = f;
		this._routes = r;
	}

	public int getLocation()
	{
		return this._location;
	}

	public int[] getFuel()
	{
		return this._fuel;
	}

	public VehiclePathPoint[][] getRoute()
	{
		return this._routes;
	}
}
