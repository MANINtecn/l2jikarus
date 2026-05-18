package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.network.serverpackets.Attack;

public class CreatureAttackTaskManager
{
	private static final Set<Map<Creature, CreatureAttackTaskManager.ScheduledAttack>> ATTACK_POOLS = ConcurrentHashMap.newKeySet();
	private static final Set<Map<Creature, CreatureAttackTaskManager.ScheduledFinish>> FINISH_POOLS = ConcurrentHashMap.newKeySet();
 

	protected CreatureAttackTaskManager()
	{
	}

	public void onHitTimeNotDual(Creature creature, Weapon weapon, Attack attack, int hitTime, int attackTime)
	{
		this.scheduleAttack(CreatureAttackTaskManager.ScheduledAttackType.NORMAL, creature, weapon, attack, hitTime, attackTime, 0, hitTime);
	}

	public void onFirstHitTimeForDual(Creature creature, Weapon weapon, Attack attack, int hitTime, int attackTime, int delayForSecondAttack)
	{
		this.scheduleAttack(CreatureAttackTaskManager.ScheduledAttackType.DUAL_FIRST, creature, weapon, attack, hitTime, attackTime, delayForSecondAttack, hitTime);
	}

	public void onSecondHitTimeForDual(Creature creature, Weapon weapon, Attack attack, int hitTime, int attackTime, int delayForSecondAttack)
	{
		this.scheduleAttack(CreatureAttackTaskManager.ScheduledAttackType.DUAL_SECOND, creature, weapon, attack, hitTime, attackTime, delayForSecondAttack, delayForSecondAttack);
	}

	private void scheduleAttack(CreatureAttackTaskManager.ScheduledAttackType type, Creature creature, Weapon weapon, Attack attack, int hitTime, int attackTime, int delayForSecondAttack, int taskDelay)
	{
		CreatureAttackTaskManager.ScheduledAttack scheduledAttack = new CreatureAttackTaskManager.ScheduledAttack(type, weapon, attack, hitTime, attackTime, delayForSecondAttack, taskDelay + System.currentTimeMillis());

		for (Map<Creature, CreatureAttackTaskManager.ScheduledAttack> pool : ATTACK_POOLS)
		{
			if (pool.size() < 300)
			{
				pool.put(creature, scheduledAttack);
				return;
			}
		}

		Map<Creature, CreatureAttackTaskManager.ScheduledAttack> poolx = new ConcurrentHashMap<>();
		poolx.put(creature, scheduledAttack);
		ThreadPool.schedulePriorityTaskAtFixedRate(new CreatureAttackTaskManager.ScheduleAttackTask(poolx), 10L, 10L);
		ATTACK_POOLS.add(poolx);
	}

	public void onAttackFinish(Creature creature, Attack attack, int taskDelay)
	{
		CreatureAttackTaskManager.ScheduledFinish scheduledFinish = new CreatureAttackTaskManager.ScheduledFinish(attack, taskDelay + System.currentTimeMillis());

		for (Map<Creature, CreatureAttackTaskManager.ScheduledFinish> pool : FINISH_POOLS)
		{
			if (pool.size() < 300)
			{
				pool.put(creature, scheduledFinish);
				return;
			}
		}

		Map<Creature, CreatureAttackTaskManager.ScheduledFinish> poolx = new ConcurrentHashMap<>();
		poolx.put(creature, scheduledFinish);
		ThreadPool.schedulePriorityTaskAtFixedRate(new CreatureAttackTaskManager.ScheduleAbortTask(poolx), 10L, 10L);
		FINISH_POOLS.add(poolx);
	}

	public void abortAttack(Creature creature)
	{
		for (Map<Creature, CreatureAttackTaskManager.ScheduledAttack> pool : ATTACK_POOLS)
		{
			if (pool.remove(creature) != null)
			{
				break;
			}
		}

		for (Map<Creature, CreatureAttackTaskManager.ScheduledFinish> poolx : FINISH_POOLS)
		{
			if (poolx.remove(creature) != null)
			{
				return;
			}
		}
	}

	public static final CreatureAttackTaskManager getInstance()
	{
		return CreatureAttackTaskManager.SingletonHolder.INSTANCE;
	}

	private class ScheduleAbortTask implements Runnable
	{
		private final Map<Creature, CreatureAttackTaskManager.ScheduledFinish> _creatureFinishData;

		public ScheduleAbortTask(Map<Creature, CreatureAttackTaskManager.ScheduledFinish> creatureFinishData)
		{
			Objects.requireNonNull(CreatureAttackTaskManager.this);
			super();
			this._creatureFinishData = creatureFinishData;
		}

		@Override
		public void run()
		{
			if (!this._creatureFinishData.isEmpty())
			{
				long currentTime = System.currentTimeMillis();
				Iterator<Entry<Creature, CreatureAttackTaskManager.ScheduledFinish>> iterator = this._creatureFinishData.entrySet().iterator();

				while (iterator.hasNext())
				{
					Entry<Creature, CreatureAttackTaskManager.ScheduledFinish> entry = iterator.next();
					CreatureAttackTaskManager.ScheduledFinish scheduledFinish = entry.getValue();
					if (currentTime >= scheduledFinish.endTime)
					{
						iterator.remove();
						Creature creature = entry.getKey();
						creature.onAttackFinish(scheduledFinish.attack);
					}
				}
			}
		}
	}

	private class ScheduleAttackTask implements Runnable
	{
		private final Map<Creature, CreatureAttackTaskManager.ScheduledAttack> _creatureAttackData;

		public ScheduleAttackTask(Map<Creature, CreatureAttackTaskManager.ScheduledAttack> creatureattackData)
		{
			Objects.requireNonNull(CreatureAttackTaskManager.this);
			super();
			this._creatureAttackData = creatureattackData;
		}

		@Override
		public void run()
		{
			if (!this._creatureAttackData.isEmpty())
			{
				long currentTime = System.currentTimeMillis();
				Iterator<Entry<Creature, CreatureAttackTaskManager.ScheduledAttack>> iterator = this._creatureAttackData.entrySet().iterator();

				while (iterator.hasNext())
				{
					Entry<Creature, CreatureAttackTaskManager.ScheduledAttack> entry = iterator.next();
					CreatureAttackTaskManager.ScheduledAttack scheduledAttack = entry.getValue();
					if (currentTime >= scheduledAttack.endTime)
					{
						iterator.remove();
						Creature creature = entry.getKey();
						switch (scheduledAttack.type)
						{
							case NORMAL:
								creature.onHitTimeNotDual(scheduledAttack.weapon, scheduledAttack.attack, scheduledAttack.hitTime, scheduledAttack.attackTime);
								break;
							case DUAL_FIRST:
								creature.onFirstHitTimeForDual(scheduledAttack.weapon, scheduledAttack.attack, scheduledAttack.hitTime, scheduledAttack.attackTime, scheduledAttack.delayForSecondAttack);
								break;
							case DUAL_SECOND:
								creature.onSecondHitTimeForDual(scheduledAttack.weapon, scheduledAttack.attack, scheduledAttack.hitTime, scheduledAttack.delayForSecondAttack, scheduledAttack.attackTime);
						}
					}
				}
			}
		}
	}

	private class ScheduledAttack
	{
		public final CreatureAttackTaskManager.ScheduledAttackType type;
		public final Weapon weapon;
		public final Attack attack;
		public final int hitTime;
		public final int attackTime;
		public final int delayForSecondAttack;
		public final long endTime;

		public ScheduledAttack(CreatureAttackTaskManager.ScheduledAttackType type, Weapon weapon, Attack attack, int hitTime, int attackTime, int delayForSecondAttack, long endTime)
		{
			Objects.requireNonNull(CreatureAttackTaskManager.this);
			super();
			this.type = type;
			this.weapon = weapon;
			this.attack = attack;
			this.hitTime = hitTime;
			this.attackTime = attackTime;
			this.delayForSecondAttack = delayForSecondAttack;
			this.endTime = endTime;
		}
	}

	private static enum ScheduledAttackType
	{
		NORMAL,
		DUAL_FIRST,
		DUAL_SECOND;
	}

	private class ScheduledFinish
	{
		public final Attack attack;
		public final long endTime;

		public ScheduledFinish(Attack attack, long endTime)
		{
			Objects.requireNonNull(CreatureAttackTaskManager.this);
			super();
			this.attack = attack;
			this.endTime = endTime;
		}
	}

	private static class SingletonHolder
	{
		protected static final CreatureAttackTaskManager INSTANCE = new CreatureAttackTaskManager();
	}
}
