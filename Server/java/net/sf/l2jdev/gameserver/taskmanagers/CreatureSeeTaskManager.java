package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public class CreatureSeeTaskManager implements Runnable
{
	private static final Set<Creature> CREATURES = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;

	protected CreatureSeeTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
	}

	@Override
	public void run()
	{
		if (!_working)
		{
			_working = true;

			for (Creature creature : CREATURES)
			{
				creature.updateSeenCreatures();
			}

			_working = false;
		}
	}

	public void add(Creature creature)
	{
		CREATURES.add(creature);
	}

	public void remove(Creature creature)
	{
		CREATURES.remove(creature);
	}

	public static CreatureSeeTaskManager getInstance()
	{
		return CreatureSeeTaskManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CreatureSeeTaskManager INSTANCE = new CreatureSeeTaskManager();
	}
}
