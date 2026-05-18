package net.sf.l2jdev.gameserver.network.clientpackets.subjugation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.SubjugationGacha;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerPurgeHolder;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemPurgeReward;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.subjugation.ExSubjugationGacha;
import net.sf.l2jdev.gameserver.network.serverpackets.subjugation.ExSubjugationGachaUI;

public class RequestSubjugationGacha extends ClientPacket
{
	private int _category;
	private int _amount;

	@Override
	protected void readImpl()
	{
		this._category = this.readInt();
		this._amount = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		if (this._amount >= 1 && this._amount * 20000L >= 1L)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				PlayerPurgeHolder playerKeys = player.getPurgePoints().get(this._category);
				Map<Integer, Double> subjugationData = SubjugationGacha.getInstance().getSubjugation(this._category);
				if (playerKeys != null && playerKeys.getKeys() >= this._amount && player.getInventory().getAdena() > 20000L * this._amount)
				{
					player.getInventory().reduceAdena(ItemProcessType.FEE, 20000L * this._amount, player, null);
					int curKeys = playerKeys.getKeys() - this._amount;
					player.getPurgePoints().put(this._category, new PlayerPurgeHolder(playerKeys.getPoints(), curKeys, 0));
					Map<Integer, Integer> rewards = new HashMap<>();

					for (int i = 0; i < this._amount; i++)
					{
						double rate = 0.0;

						for (int index = 0; index < subjugationData.size(); index++)
						{
							double[] chances = subjugationData.values().stream().mapToDouble(it -> it).toArray();
							double maxBound = Arrays.stream(chances).sum();
							double itemChance = chances[index];
							if (Rnd.get(maxBound - rate) < itemChance)
							{
								int itemId = subjugationData.keySet().stream().mapToInt(it -> it).toArray()[index];
								rewards.put(itemId, rewards.getOrDefault(itemId, 0) + 1);
								Item item = player.addItem(ItemProcessType.REWARD, itemId, 1L, player, true);
								if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_PURGE_REWARD))
								{
									EventDispatcher.getInstance().notifyEventAsync(new OnItemPurgeReward(player, item));
								}
								break;
							}

							rate += itemChance;
						}
					}

					player.sendPacket(new ExSubjugationGachaUI(this._category, curKeys));
					player.sendPacket(new ExSubjugationGacha(rewards));
				}
			}
		}
	}
}
