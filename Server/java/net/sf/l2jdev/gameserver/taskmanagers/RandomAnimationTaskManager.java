package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Npc;

public class RandomAnimationTaskManager implements Runnable
{
	private static final Map<Npc, Long> PENDING_ANIMATIONS = new ConcurrentHashMap<>();
	private static boolean _working = false;

	protected RandomAnimationTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 0L, 1000L);
	}

	@Override
	public void run()
	{
		if (!_working)
		{
			_working = true;
			long currentTime = System.currentTimeMillis();

			for (Entry<Npc, Long> entry : PENDING_ANIMATIONS.entrySet())
			{
				if (currentTime > entry.getValue())
				{
					Npc npc = entry.getKey();
					if (npc.isInActiveRegion() && !npc.isDead() && !npc.isInCombat() && !npc.isMoving() && !npc.hasBlockActions())
					{
						npc.onRandomAnimation(Rnd.get(2, 3));
					}

					PENDING_ANIMATIONS.put(npc, currentTime + Rnd.get(npc.isAttackable() ? GeneralConfig.MIN_MONSTER_ANIMATION : GeneralConfig.MIN_NPC_ANIMATION, npc.isAttackable() ? GeneralConfig.MAX_MONSTER_ANIMATION : GeneralConfig.MAX_NPC_ANIMATION) * 1000);
				}
			}

			_working = false;
		}
	}

	public void add(Npc npc)
	{
		if (npc.hasRandomAnimation())
		{
			PENDING_ANIMATIONS.putIfAbsent(npc, System.currentTimeMillis() + Rnd.get(npc.isAttackable() ? GeneralConfig.MIN_MONSTER_ANIMATION : GeneralConfig.MIN_NPC_ANIMATION, npc.isAttackable() ? GeneralConfig.MAX_MONSTER_ANIMATION : GeneralConfig.MAX_NPC_ANIMATION) * 1000);
		}
	}

	public void remove(Npc npc)
	{
		PENDING_ANIMATIONS.remove(npc);
	}

	public static RandomAnimationTaskManager getInstance()
	{
		return RandomAnimationTaskManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RandomAnimationTaskManager INSTANCE = new RandomAnimationTaskManager();
	}
}
