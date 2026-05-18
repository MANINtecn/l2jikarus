package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.zone.ZoneForm;

public class BannedSpawnTerritory
{
	private final String _name;
	private final ZoneForm _territory;

	public BannedSpawnTerritory(String name, ZoneForm territory)
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

	public boolean isInsideZone(int x, int y, int z)
	{
		return this._territory.isInsideZone(x, y, z);
	}

	public void visualizeZone(int z)
	{
		this._territory.visualizeZone(z);
	}
}
