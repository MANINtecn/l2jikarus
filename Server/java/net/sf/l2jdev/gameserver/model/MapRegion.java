package net.sf.l2jdev.gameserver.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;

public class MapRegion
{
	private final String _name;
	private final String _town;
	private final int _locId;
	private final int _bbs;
	private List<int[]> _maps = null;
	private List<Location> _spawnLocs = null;
	private List<Location> _otherSpawnLocs = null;
	private List<Location> _chaoticSpawnLocs = null;
	private List<Location> _banishSpawnLocs = null;
	private final Map<Race, String> _bannedRace = new EnumMap<>(Race.class);

	public MapRegion(String name, String town, int locId, int bbs)
	{
		this._name = name;
		this._town = town;
		this._locId = locId;
		this._bbs = bbs;
	}

	public String getName()
	{
		return this._name;
	}

	public String getTown()
	{
		return this._town;
	}

	public int getLocId()
	{
		return this._locId;
	}

	public int getBbs()
	{
		return this._bbs;
	}

	public void addMap(int x, int y)
	{
		if (this._maps == null)
		{
			this._maps = new ArrayList<>();
		}

		this._maps.add(new int[]
		{
			x,
			y
		});
	}

	public List<int[]> getMaps()
	{
		return this._maps;
	}

	public boolean isZoneInRegion(int x, int y)
	{
		if (this._maps == null)
		{
			return false;
		}
		for (int[] map : this._maps)
		{
			if (map[0] == x && map[1] == y)
			{
				return true;
			}
		}

		return false;
	}

	public void addSpawn(int x, int y, int z)
	{
		if (this._spawnLocs == null)
		{
			this._spawnLocs = new ArrayList<>();
		}

		this._spawnLocs.add(new Location(x, y, z));
	}

	public void addOtherSpawn(int x, int y, int z)
	{
		if (this._otherSpawnLocs == null)
		{
			this._otherSpawnLocs = new ArrayList<>();
		}

		this._otherSpawnLocs.add(new Location(x, y, z));
	}

	public void addChaoticSpawn(int x, int y, int z)
	{
		if (this._chaoticSpawnLocs == null)
		{
			this._chaoticSpawnLocs = new ArrayList<>();
		}

		this._chaoticSpawnLocs.add(new Location(x, y, z));
	}

	public void addBanishSpawn(int x, int y, int z)
	{
		if (this._banishSpawnLocs == null)
		{
			this._banishSpawnLocs = new ArrayList<>();
		}

		this._banishSpawnLocs.add(new Location(x, y, z));
	}

	public List<Location> getSpawns()
	{
		return this._spawnLocs;
	}

	public Location getSpawnLoc()
	{
		return PlayerConfig.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._spawnLocs.get(Rnd.get(this._spawnLocs.size())) : this._spawnLocs.get(0);
	}

	public Location getOtherSpawnLoc()
	{
		if (this._otherSpawnLocs != null)
		{
			return PlayerConfig.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._otherSpawnLocs.get(Rnd.get(this._otherSpawnLocs.size())) : this._otherSpawnLocs.get(0);
		}
		return this.getSpawnLoc();
	}

	public Location getChaoticSpawnLoc()
	{
		if (this._chaoticSpawnLocs != null)
		{
			return PlayerConfig.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._chaoticSpawnLocs.get(Rnd.get(this._chaoticSpawnLocs.size())) : this._chaoticSpawnLocs.get(0);
		}
		return this.getSpawnLoc();
	}

	public Location getBanishSpawnLoc()
	{
		if (this._banishSpawnLocs != null)
		{
			return PlayerConfig.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._banishSpawnLocs.get(Rnd.get(this._banishSpawnLocs.size())) : this._banishSpawnLocs.get(0);
		}
		return this.getSpawnLoc();
	}

	public void addBannedRace(String race, String point)
	{
		this._bannedRace.put(Race.valueOf(race), point);
	}

	public Map<Race, String> getBannedRace()
	{
		return this._bannedRace;
	}
}
