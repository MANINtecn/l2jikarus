package net.sf.l2jdev.gameserver.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.data.holders.SharedTeleportHolder;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public class SharedTeleportManager
{
	protected static final Logger LOGGER = Logger.getLogger(SharedTeleportManager.class.getName());
	public static final int TELEPORT_COUNT = 5;
	private final Map<Integer, SharedTeleportHolder> _sharedTeleports = new ConcurrentHashMap<>();
	private int _lastSharedTeleportId = 0;

	protected SharedTeleportManager()
	{
		LOGGER.info(this.getClass().getSimpleName() + ": initialized.");
	}

	public SharedTeleportHolder getTeleport(int id)
	{
		return this._sharedTeleports.get(id);
	}

	public synchronized int nextId(Creature creature)
	{
		int nextId = ++this._lastSharedTeleportId;
		this._sharedTeleports.put(nextId, new SharedTeleportHolder(nextId, creature.getName(), 5, creature.getX(), creature.getY(), creature.getZ()));
		return nextId;
	}

	public static SharedTeleportManager getInstance()
	{
		return SharedTeleportManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SharedTeleportManager INSTANCE = new SharedTeleportManager();
	}
}
