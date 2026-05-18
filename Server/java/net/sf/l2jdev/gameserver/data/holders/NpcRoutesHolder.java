package net.sf.l2jdev.gameserver.data.holders;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;

public class NpcRoutesHolder
{
	private final Map<String, String> _correspondences = new HashMap<>();

	public void addRoute(String routeName, Location loc)
	{
		this._correspondences.put(getUniqueKey(loc), routeName);
	}

	public String getRouteName(Npc npc)
	{
		if (npc.getSpawn() != null)
		{
			String key = getUniqueKey(npc.getSpawn().getLocation());
			return this._correspondences.containsKey(key) ? this._correspondences.get(key) : "";
		}
		return "";
	}

	private static String getUniqueKey(ILocational loc)
	{
		return loc.getX() + "-" + loc.getY() + "-" + loc.getZ();
	}
}
