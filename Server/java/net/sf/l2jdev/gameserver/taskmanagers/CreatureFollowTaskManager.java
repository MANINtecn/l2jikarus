package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public class CreatureFollowTaskManager
{
	protected static final Map<Creature, Integer> NORMAL_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	protected static final Map<Creature, Integer> ATTACK_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	protected static boolean _workingNormal = false;
	protected static boolean _workingAttack = false;

	protected CreatureFollowTaskManager()
	{
		ThreadPool.schedulePriorityTaskAtFixedRate(new CreatureFollowTaskManager.CreatureFollowNormalTask(), 1000L, 1000L);
		ThreadPool.schedulePriorityTaskAtFixedRate(new CreatureFollowTaskManager.CreatureFollowAttackTask(), 500L, 500L);
	}

	protected void follow(Creature creature, int range)
	{
		try
		{
			if (creature.hasAI())
			{
				CreatureAI ai = creature.getAI();
				if (ai != null)
				{
					WorldObject followTarget = ai.getTarget();
					if (followTarget == null)
					{
						if (creature.isSummon())
						{
							creature.asSummon().setFollowStatus(false);
						}

						ai.setIntention(Intention.IDLE);
						return;
					}

					int followRange = range == -1 ? Rnd.get(50, 100) : range;
					if (!creature.isInsideRadius3D(followTarget, followRange))
					{
						if (!creature.isInsideRadius3D(followTarget, 3000))
						{
							if (creature.isSummon())
							{
								creature.asSummon().setFollowStatus(false);
							}

							ai.setIntention(Intention.IDLE);
							return;
						}

						ai.moveToPawn(followTarget, followRange);
					}
				}
				else
				{
					this.remove(creature);
				}
			}
			else
			{
				this.remove(creature);
			}
		}
		catch (Exception var6)
		{
		}
	}

	public boolean isFollowing(Creature creature)
	{
		return NORMAL_FOLLOW_CREATURES.containsKey(creature) || ATTACK_FOLLOW_CREATURES.containsKey(creature);
	}

	public void addNormalFollow(Creature creature, int range)
	{
		NORMAL_FOLLOW_CREATURES.putIfAbsent(creature, range);
		this.follow(creature, range);
	}

	public void addAttackFollow(Creature creature, int range)
	{
		ATTACK_FOLLOW_CREATURES.putIfAbsent(creature, range);
		this.follow(creature, range);
	}

	public void remove(Creature creature)
	{
		NORMAL_FOLLOW_CREATURES.remove(creature);
		ATTACK_FOLLOW_CREATURES.remove(creature);
	}

	public static CreatureFollowTaskManager getInstance()
	{
		return CreatureFollowTaskManager.SingletonHolder.INSTANCE;
	}

	protected class CreatureFollowAttackTask implements Runnable
	{
		protected CreatureFollowAttackTask()
		{
			Objects.requireNonNull(CreatureFollowTaskManager.this);
			super();
		}

		@Override
		public void run()
		{
			if (!CreatureFollowTaskManager._workingAttack)
			{
				CreatureFollowTaskManager._workingAttack = true;
				if (!CreatureFollowTaskManager.ATTACK_FOLLOW_CREATURES.isEmpty())
				{
					for (Entry<Creature, Integer> entry : CreatureFollowTaskManager.ATTACK_FOLLOW_CREATURES.entrySet())
					{
						CreatureFollowTaskManager.this.follow(entry.getKey(), entry.getValue());
					}
				}

				CreatureFollowTaskManager._workingAttack = false;
			}
		}
	}

	protected class CreatureFollowNormalTask implements Runnable
	{
		protected CreatureFollowNormalTask()
		{
			Objects.requireNonNull(CreatureFollowTaskManager.this);
			super();
		}

		@Override
		public void run()
		{
			if (!CreatureFollowTaskManager._workingNormal)
			{
				CreatureFollowTaskManager._workingNormal = true;
				if (!CreatureFollowTaskManager.NORMAL_FOLLOW_CREATURES.isEmpty())
				{
					for (Entry<Creature, Integer> entry : CreatureFollowTaskManager.NORMAL_FOLLOW_CREATURES.entrySet())
					{
						CreatureFollowTaskManager.this.follow(entry.getKey(), entry.getValue());
					}
				}

				CreatureFollowTaskManager._workingNormal = false;
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final CreatureFollowTaskManager INSTANCE = new CreatureFollowTaskManager();
	}
}
