package net.sf.l2jdev.gameserver.network.clientpackets.randomcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.gameserver.config.RandomCraftConfig;
import net.sf.l2jdev.gameserver.data.xml.RandomCraftData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.RandomCraftRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftExtract;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftInfo;

public class ExRequestRandomCraftExtract extends ClientPacket
{
	private final Map<Integer, Long> _items = new HashMap<>();

	@Override
	protected void readImpl()
	{
		int size = this.readInt();

		for (int i = 0; i < size; i++)
		{
			int objId = this.readInt();
			long count = this.readLong();
			if (count > 0L)
			{
				this._items.put(objId, count);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		if (RandomCraftConfig.ENABLE_RANDOM_CRAFT)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (!player.hasItemRequest() && !player.hasRequest(RandomCraftRequest.class))
				{
					player.addRequest(new RandomCraftRequest(player));
					int points = 0;
					int fee = 0;
					Map<Integer, Long> toDestroy = new HashMap<>();

					for (Entry<Integer, Long> e : this._items.entrySet())
					{
						int objId = e.getKey();
						long count = e.getValue();
						if (count < 1L)
						{
							player.removeRequest(RandomCraftRequest.class);
							return;
						}

						Item item = player.getInventory().getItemByObjectId(objId);
						if (item != null)
						{
							if (count <= item.getCount())
							{
								toDestroy.put(objId, count);
								points = (int) (points + RandomCraftData.getInstance().getPoints(item.getId()) * count);
								fee = (int) (fee + RandomCraftData.getInstance().getFee(item.getId()) * count);
							}
						}
						else
						{
							player.sendPacket(new ExCraftExtract());
						}
					}

					if (points >= 1 && fee >= 0)
					{
						if (player.reduceAdena(ItemProcessType.FEE, fee, player, true))
						{
							for (Entry<Integer, Long> e : toDestroy.entrySet())
							{
								player.destroyItem(ItemProcessType.FEE, e.getKey(), e.getValue(), player, true);
							}

							player.getRandomCraft().addCraftPoints(points);
						}

						player.sendPacket(new ExCraftInfo(player));
						player.sendPacket(new ExCraftExtract());
						player.removeRequest(RandomCraftRequest.class);
					}
					else
					{
						player.removeRequest(RandomCraftRequest.class);
					}
				}
			}
		}
	}
}
