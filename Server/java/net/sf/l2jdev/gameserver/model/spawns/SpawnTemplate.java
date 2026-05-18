package net.sf.l2jdev.gameserver.model.spawns;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.IParameterized;
import net.sf.l2jdev.gameserver.model.interfaces.ITerritorized;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.zone.type.BannedSpawnTerritory;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;

public class SpawnTemplate implements Cloneable, ITerritorized, IParameterized<StatSet>
{
	private final String _name;
	private final String _ai;
	private final boolean _spawnByDefault;
	private final File _file;
	private List<SpawnTerritory> _territories;
	private List<BannedSpawnTerritory> _bannedTerritories;
	private final List<SpawnGroup> _groups = new ArrayList<>();
	private StatSet _parameters;

	public SpawnTemplate(StatSet set, File file)
	{
		this(set.getString("name", null), set.getString("ai", null), set.getBoolean("spawnByDefault", true), file);
	}

	private SpawnTemplate(String name, String ai, boolean spawnByDefault, File file)
	{
		this._name = name;
		this._ai = ai;
		this._spawnByDefault = spawnByDefault;
		this._file = file;
	}

	public String getName()
	{
		return this._name;
	}

	public String getAI()
	{
		return this._ai;
	}

	public boolean isSpawningByDefault()
	{
		return this._spawnByDefault;
	}

	public File getFile()
	{
		return this._file;
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

	public void addGroup(SpawnGroup group)
	{
		this._groups.add(group);
	}

	public List<SpawnGroup> getGroups()
	{
		return this._groups;
	}

	public List<SpawnGroup> getGroupsByName(String name)
	{
		List<SpawnGroup> result = new ArrayList<>();

		for (SpawnGroup group : this._groups)
		{
			if (group.getName() != null && group.getName().equalsIgnoreCase(name))
			{
				result.add(group);
			}
		}

		return result;
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

	public void notifyEvent(Consumer<Quest> event)
	{
		if (this._ai != null)
		{
			Quest script = ScriptManager.getInstance().getScript(this._ai);
			if (script != null)
			{
				event.accept(script);
			}
		}
	}

	public void spawn(Predicate<SpawnGroup> groupFilter, Instance instance)
	{
		for (SpawnGroup group : this._groups)
		{
			if (groupFilter.test(group))
			{
				group.spawnAll(instance);
			}
		}
	}

	public void spawnAll()
	{
		this.spawnAll(null);
	}

	public void spawnAll(Instance instance)
	{
		this.spawn(SpawnGroup::isSpawningByDefault, instance);
	}

	public void notifyActivate()
	{
		this.notifyEvent(script -> script.onSpawnActivate(this));
	}

	public void spawnAllIncludingNotDefault(Instance instance)
	{
		this._groups.forEach(group -> group.spawnAll(instance));
	}

	public void despawn(Predicate<SpawnGroup> groupFilter)
	{
		for (SpawnGroup group : this._groups)
		{
			if (groupFilter.test(group))
			{
				group.despawnAll();
			}
		}

		this.notifyEvent(script -> script.onSpawnDeactivate(this));
	}

	public void despawnAll()
	{
		this._groups.forEach(SpawnGroup::despawnAll);
		this.notifyEvent(script -> script.onSpawnDeactivate(this));
	}

	@Override
	public SpawnTemplate clone()
	{
		SpawnTemplate template = new SpawnTemplate(this._name, this._ai, this._spawnByDefault, this._file);
		template.setParameters(this._parameters);

		for (BannedSpawnTerritory territory : this.getBannedTerritories())
		{
			template.addBannedTerritory(territory);
		}

		for (SpawnTerritory territory : this.getTerritories())
		{
			template.addTerritory(territory);
		}

		for (SpawnGroup group : this._groups)
		{
			template.addGroup(group.clone());
		}

		return template;
	}
}
