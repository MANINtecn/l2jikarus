package net.sf.l2jdev.gameserver.managers.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.EventDropHolder;
import net.sf.l2jdev.gameserver.model.script.LongTimeEvent;

public class EventDropManager
{
	private static final Map<LongTimeEvent, List<EventDropHolder>> EVENT_DROPS = new ConcurrentHashMap<>(1);

	public void addDrops(LongTimeEvent longTimeEvent, List<EventDropHolder> dropList)
	{
		EVENT_DROPS.put(longTimeEvent, dropList);
	}

	public void removeDrops(LongTimeEvent longTimeEvent)
	{
		EVENT_DROPS.remove(longTimeEvent);
	}

	public void doEventDrop(Creature attacker, Attackable attackable)
	{
		if (!EVENT_DROPS.isEmpty())
		{
			if (attacker != null && attacker.isPlayable() && !attackable.isFakePlayer())
			{
				Player player = attacker.asPlayer();
				if (player.getLevel() - attackable.getLevel() <= RatesConfig.EVENT_ITEM_MAX_LEVEL_LOWEST_DIFFERENCE)
				{
					for (List<EventDropHolder> eventDrops : EVENT_DROPS.values())
					{
						for (EventDropHolder drop : eventDrops)
						{
							if (drop.getMonsterIds().isEmpty() || drop.getMonsterIds().contains(attackable.getId()))
							{
								int monsterLevel = attackable.getLevel();
								if (monsterLevel >= drop.getMinLevel() && monsterLevel <= drop.getMaxLevel() && Rnd.get(100.0) < drop.getChance())
								{
									int itemId = drop.getItemId();
									long itemCount = Rnd.get(drop.getMin(), drop.getMax());
									if (!PlayerConfig.AUTO_LOOT_ITEM_IDS.contains(itemId) && !PlayerConfig.AUTO_LOOT && !attackable.isFlying())
									{
										attackable.dropItem(player, itemId, itemCount);
									}
									else
									{
										player.doAutoLoot(attackable, itemId, itemCount);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static EventDropManager getInstance()
	{
		return EventDropManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EventDropManager INSTANCE = new EventDropManager();
	}
}
