package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.zone.ZoneForm;

public class SpawnTerritory
{
	private final String _name;
	private final ZoneForm _territory;

	public SpawnTerritory(String name, ZoneForm territory)
	{
		this._name = name;
		this._territory = territory;
	}

	public String getName()
	{
		return this._name;
	}

	public Location getRandomPoint()
	{
		return this._territory.getRandomPoint();
	}

	public Location getCenterPoint()
	{
		return this._territory.getCenterPoint();
	}

	public boolean isInsideZone(int x, int y, int z)
	{
		return this._territory.isInsideZone(x, y, z);
	}

	public int getHighZ()
	{
		return this._territory.getHighZ();
	}

	public void visualizeZone(int z)
	{
		this._territory.visualizeZone(z);
	}
}
