package net.sf.l2jdev.gameserver.model.interfaces;

import java.util.List;

import net.sf.l2jdev.gameserver.model.zone.type.BannedSpawnTerritory;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;

public interface ITerritorized
{
	void addTerritory(SpawnTerritory var1);

	List<SpawnTerritory> getTerritories();

	void addBannedTerritory(BannedSpawnTerritory var1);

	List<BannedSpawnTerritory> getBannedTerritories();
}
