package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class ItemsAutoDestroyTaskManager implements Runnable
{
	private static final Set<Item> ITEMS = ConcurrentHashMap.newKeySet();

	protected ItemsAutoDestroyTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 5000L, 5000L);
	}

	@Override
	public void run()
	{
		if (!ITEMS.isEmpty())
		{
			long currentTime = System.currentTimeMillis();
			Iterator<Item> iterator = ITEMS.iterator();

			while (iterator.hasNext())
			{
				Item itemInstance = iterator.next();
				if (itemInstance.getDropTime() != 0L && itemInstance.getItemLocation() == ItemLocation.VOID)
				{
					long autoDestroyTime;
					if (itemInstance.getTemplate().getAutoDestroyTime() > 0)
					{
						autoDestroyTime = itemInstance.getTemplate().getAutoDestroyTime();
					}
					else if (itemInstance.getTemplate().hasExImmediateEffect())
					{
						autoDestroyTime = GeneralConfig.HERB_AUTO_DESTROY_TIME;
					}
					else
					{
						autoDestroyTime = GeneralConfig.AUTODESTROY_ITEM_AFTER == 0 ? 3600000 : GeneralConfig.AUTODESTROY_ITEM_AFTER * 1000;
					}

					if (currentTime - itemInstance.getDropTime() > autoDestroyTime)
					{
						itemInstance.decayMe();
						if (GeneralConfig.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(itemInstance);
						}

						iterator.remove();
					}
				}
				else
				{
					iterator.remove();
				}
			}
		}
	}

	public void addItem(Item item)
	{
		item.setDropTime(System.currentTimeMillis());
		ITEMS.add(item);
	}

	public static ItemsAutoDestroyTaskManager getInstance()
	{
		return ItemsAutoDestroyTaskManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroyTaskManager INSTANCE = new ItemsAutoDestroyTaskManager();
	}
}
