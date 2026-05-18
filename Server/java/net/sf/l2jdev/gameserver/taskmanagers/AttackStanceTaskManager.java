package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.network.serverpackets.AutoAttackStop;

public class AttackStanceTaskManager implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(AttackStanceTaskManager.class.getName());
	public static final long COMBAT_TIME = 15000L;
	private static final Map<Creature, Long> CREATURE_ATTACK_STANCES = new ConcurrentHashMap<>();
	private static boolean _working = false;

	protected AttackStanceTaskManager()
	{
		ThreadPool.schedulePriorityTaskAtFixedRate(this, 0L, 1000L);
	}

	@Override
	public void run()
	{
		if (!_working)
		{
			_working = true;
			if (!CREATURE_ATTACK_STANCES.isEmpty())
			{
				try
				{
					long currentTime = System.currentTimeMillis();
					Iterator<Entry<Creature, Long>> iterator = CREATURE_ATTACK_STANCES.entrySet().iterator();

					while (iterator.hasNext())
					{
						Entry<Creature, Long> entry = iterator.next();
						if (currentTime - entry.getValue() > 15000L)
						{
							Creature creature = entry.getKey();
							if (creature != null)
							{
								creature.broadcastPacket(new AutoAttackStop(creature.getObjectId()));
								creature.getAI().setAutoAttacking(false);
								if (creature.isPlayer() && creature.hasSummon())
								{
									creature.asPlayer().clearDamageTaken();
									Summon pet = creature.getPet();
									if (pet != null)
									{
										pet.broadcastPacket(new AutoAttackStop(pet.getObjectId()));
									}

									creature.getServitors().values().forEach(s -> s.broadcastPacket(new AutoAttackStop(s.getObjectId())));
								}
							}

							iterator.remove();
						}
					}
				}
				catch (Exception var7)
				{
					LOGGER.log(Level.WARNING, "Error in AttackStanceTaskManager: " + var7.getMessage(), var7);
				}
			}

			_working = false;
		}
	}

	public void addAttackStanceTask(Creature creature)
	{
		if (creature != null)
		{
			CREATURE_ATTACK_STANCES.put(creature, System.currentTimeMillis());
		}
	}

	public void removeAttackStanceTask(Creature creature)
	{
		Creature actor = creature;
		if (creature != null)
		{
			if (creature.isSummon())
			{
				actor = creature.asPlayer();
			}

			CREATURE_ATTACK_STANCES.remove(actor);
		}
	}

	public boolean hasAttackStanceTask(Creature creature)
	{
		Creature actor = creature;
		if (creature != null)
		{
			if (creature.isSummon())
			{
				actor = creature.asPlayer();
			}

			return CREATURE_ATTACK_STANCES.containsKey(actor);
		}
		return false;
	}

	public static AttackStanceTaskManager getInstance()
	{
		return AttackStanceTaskManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager INSTANCE = new AttackStanceTaskManager();
	}
}
