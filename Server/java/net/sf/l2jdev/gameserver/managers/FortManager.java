package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Fort;

public class FortManager
{
	public static final int ORC_FORTRESS = 122;
	public static final int ORC_FORTRESS_FLAG = 93331;
	public static final int FLAG_MAX_COUNT = 3;
	public static final int ORC_FORTRESS_FLAGPOLE_ID = 23170500;
	private static final Logger LOGGER = Logger.getLogger(FortManager.class.getName());
	private static final Map<Integer, Fort> _forts = new ConcurrentSkipListMap<>();

	public Fort findNearestFort(WorldObject obj)
	{
		return this.findNearestFort(obj, Long.MAX_VALUE);
	}

	public Fort findNearestFort(WorldObject obj, long maxDistanceValue)
	{
		Fort nearestFort = this.getFort(obj);
		if (nearestFort == null)
		{
			long maxDistance = maxDistanceValue;

			for (Fort fort : _forts.values())
			{
				double distance = fort.getDistance(obj);
				if (maxDistance > distance)
				{
					maxDistance = (long) distance;
					nearestFort = fort;
				}
			}
		}

		return nearestFort;
	}

	public Fort getFortById(int fortId)
	{
		for (Fort fort : _forts.values())
		{
			if (fort.getResidenceId() == fortId)
			{
				return fort;
			}
		}

		return null;
	}

	public Fort getFortByOwner(Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		for (Fort fort : _forts.values())
		{
			if (fort.getOwnerClan() == clan)
			{
				return fort;
			}
		}

		return null;
	}

	public Fort getFort(String name)
	{
		for (Fort fort : _forts.values())
		{
			if (fort.getName().equalsIgnoreCase(name.trim()))
			{
				return fort;
			}
		}

		return null;
	}

	public Fort getFort(int x, int y, int z)
	{
		for (Fort fort : _forts.values())
		{
			if (fort.checkIfInZone(x, y, z))
			{
				return fort;
			}
		}

		return null;
	}

	public Fort getFort(WorldObject activeObject)
	{
		return this.getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public Collection<Fort> getForts()
	{
		return _forts.values();
	}

	public void loadInstances()
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT id FROM fort ORDER BY id");)
		{
			while (rs.next())
			{
				int fortId = rs.getInt("id");
				_forts.put(fortId, new Fort(fortId));
			}

			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _forts.values().size() + " fortress.");

			for (Fort fort : _forts.values())
			{
				fort.getSiege().loadSiegeGuard();
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Exception: loadFortData(): " + var12.getMessage(), var12);
		}
	}

	public void activateInstances()
	{
		for (Fort fort : _forts.values())
		{
			fort.activateInstance();
		}
	}

	public static FortManager getInstance()
	{
		return FortManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final FortManager INSTANCE = new FortManager();
	}
}
