package net.sf.l2jdev.gameserver.model.spawns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.IParameterized;
import net.sf.l2jdev.gameserver.model.interfaces.ITerritorized;
import net.sf.l2jdev.gameserver.model.zone.type.BannedSpawnTerritory;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;

public class SpawnGroup implements Cloneable, ITerritorized, IParameterized<StatSet>
{
	private final String _name;
	private final boolean _spawnByDefault;
	private List<SpawnTerritory> _territories;
	private List<BannedSpawnTerritory> _bannedTerritories;
	private final List<NpcSpawnTemplate> _spawns = new ArrayList<>();
	private StatSet _parameters;

	public SpawnGroup(StatSet set)
	{
		this(set.getString("name", null), set.getBoolean("spawnByDefault", true));
	}

	private SpawnGroup(String name, boolean spawnByDefault)
	{
		this._name = name;
		this._spawnByDefault = spawnByDefault;
	}

	public String getName()
	{
		return this._name;
	}

	public boolean isSpawningByDefault()
	{
		return this._spawnByDefault;
	}

	public void addSpawn(NpcSpawnTemplate template)
	{
		this._spawns.add(template);
	}

	public List<NpcSpawnTemplate> getSpawns()
	{
		return this._spawns;
	}

	@Override
	public void addTerritory(SpawnTerritory territory)
	{
		if (this._territories == null)
		{
			this._territories = new ArrayList<>();
		}

		this._territories.add(territory);
	}

	@Override
	public List<SpawnTerritory> getTerritories()
	{
		return this._territories != null ? this._territories : Collections.emptyList();
	}

	@Override
	public void addBannedTerritory(BannedSpawnTerritory territory)
	{
		if (this._bannedTerritories == null)
		{
			this._bannedTerritories = new ArrayList<>();
		}

		this._bannedTerritories.add(territory);
	}

	@Override
	public List<BannedSpawnTerritory> getBannedTerritories()
	{
		return this._bannedTerritories != null ? this._bannedTerritories : Collections.emptyList();
	}

	@Override
	public StatSet getParameters()
	{
		return this._parameters;
	}

	@Override
	public void setParameters(StatSet parameters)
	{
		this._parameters = parameters;
	}

	public List<NpcSpawnTemplate> getSpawnsById(int id)
	{
		List<NpcSpawnTemplate> result = new ArrayList<>();

		for (NpcSpawnTemplate spawn : this._spawns)
		{
			if (spawn.getId() == id)
			{
				result.add(spawn);
			}
		}

		return result;
	}

	public void spawnAll()
	{
		this.spawnAll(null);
	}

	public void spawnAll(Instance instance)
	{
		this._spawns.forEach(template -> template.spawn(instance));
	}

	public void despawnAll()
	{
		this._spawns.forEach(NpcSpawnTemplate::despawn);
	}

	@Override
	public SpawnGroup clone()
	{
		SpawnGroup group = new SpawnGroup(this._name, this._spawnByDefault);

		for (BannedSpawnTerritory territory : this.getBannedTerritories())
		{
			group.addBannedTerritory(territory);
		}

		for (SpawnTerritory territory : this.getTerritories())
		{
			group.addTerritory(territory);
		}

		for (NpcSpawnTemplate spawn : this._spawns)
		{
			group.addSpawn(spawn.clone());
		}

		return group;
	}
}
