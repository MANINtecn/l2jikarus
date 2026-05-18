package net.sf.l2jdev.gameserver.network.clientpackets.luckygame;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.holders.LuckyGameDataHolder;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.LuckyGameData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameItemType;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameResultType;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameType;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.luckygame.ExBettingLuckyGameResult;
import net.sf.l2jdev.gameserver.util.MathUtil;

public class RequestLuckyGamePlay extends ClientPacket
{
	 
	private LuckyGameType _type;
	private int _reading;

	@Override
	protected void readImpl()
	{
		int type = MathUtil.clamp(this.readInt(), 0, LuckyGameType.values().length);
		this._type = LuckyGameType.values()[type];
		this._reading = MathUtil.clamp(this.readInt(), 0, 50);
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int index = this._type == LuckyGameType.LUXURY ? 102 : 2;
			LuckyGameDataHolder holder = LuckyGameData.getInstance().getLuckyGameDataByIndex(index);
			if (holder != null)
			{
				long tickets = this._type == LuckyGameType.LUXURY ? player.getInventory().getInventoryItemCount(23768, -1) : player.getInventory().getInventoryItemCount(23767, -1);
				if (tickets < this._reading)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_TICKETS_YOU_CANNOT_CONTINUE_THE_GAME);
					player.sendPacket(this._type == LuckyGameType.LUXURY ? ExBettingLuckyGameResult.LUXURY_INVALID_ITEM_COUNT : ExBettingLuckyGameResult.NORMAL_INVALID_ITEM_COUNT);
				}
				else
				{
					int playCount = player.getVariables().getInt("FortuneTelling", 0);
					boolean blackCat = player.getVariables().getBoolean("FortuneTellingBlackCat", false);
					EnumMap<LuckyGameItemType, List<ItemHolder>> rewards = new EnumMap<>(LuckyGameItemType.class);

					for (int i = 0; i < this._reading; i++)
					{
						double chance = 100.0 * Rnd.nextDouble();
						double totalChance = 0.0;

						for (ItemChanceHolder item : holder.getCommonReward())
						{
							totalChance += item.getChance();
							if (totalChance >= chance)
							{
								rewards.computeIfAbsent(LuckyGameItemType.COMMON, _ -> new ArrayList<>()).add(item);
								break;
							}
						}

						playCount++;
						if (playCount >= holder.getMinModifyRewardGame() && playCount <= holder.getMaxModifyRewardGame() && !blackCat)
						{
							List<ItemChanceHolder> modifyReward = holder.getModifyReward();
							double chanceModify = 100.0 * Rnd.nextDouble();
							totalChance = 0.0;

							for (ItemChanceHolder itemx : modifyReward)
							{
								totalChance += itemx.getChance();
								if (totalChance >= chanceModify)
								{
									rewards.computeIfAbsent(LuckyGameItemType.RARE, _ -> new ArrayList<>()).add(itemx);
									blackCat = true;
									break;
								}
							}

							if (playCount == holder.getMaxModifyRewardGame())
							{
								rewards.computeIfAbsent(LuckyGameItemType.RARE, _ -> new ArrayList<>()).add(modifyReward.get(Rnd.get(modifyReward.size())));
								blackCat = true;
							}
						}
					}

					int totalWeight = rewards.values().stream().mapToInt(list -> list.stream().mapToInt(itemxx -> ItemData.getInstance().getTemplate(itemxx.getId()).getWeight()).sum()).sum();
					if (!rewards.isEmpty() && (!player.getInventory().validateCapacity(rewards.size()) || !player.getInventory().validateWeight(totalWeight)))
					{
						player.sendPacket(this._type == LuckyGameType.LUXURY ? ExBettingLuckyGameResult.LUXURY_INVALID_CAPACITY : ExBettingLuckyGameResult.NORMAL_INVALID_CAPACITY);
						player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_EITHER_FULL_OR_OVERWEIGHT);
					}
					else if (!player.destroyItemByItemId(ItemProcessType.FEE, this._type == LuckyGameType.LUXURY ? 23768 : 23767, this._reading, player, true))
					{
						player.sendPacket(this._type == LuckyGameType.LUXURY ? ExBettingLuckyGameResult.LUXURY_INVALID_ITEM_COUNT : ExBettingLuckyGameResult.NORMAL_INVALID_ITEM_COUNT);
					}
					else
					{
						for (int i = 0; i < this._reading; i++)
						{
							int serverGameNumber = LuckyGameData.getInstance().increaseGame();
							holder.getUniqueReward().stream().filter(rewardx -> rewardx.getPoints() == serverGameNumber).forEach(itemxx -> rewards.computeIfAbsent(LuckyGameItemType.UNIQUE, _ -> new ArrayList<>()).add(itemxx));
						}

						player.sendPacket(new ExBettingLuckyGameResult(LuckyGameResultType.SUCCESS, this._type, rewards, (int) (this._type == LuckyGameType.LUXURY ? player.getInventory().getInventoryItemCount(23768, -1) : player.getInventory().getInventoryItemCount(23767, -1))));

						for (Entry<LuckyGameItemType, List<ItemHolder>> reward : rewards.entrySet())
						{
							for (ItemHolder r : reward.getValue())
							{
								Item itemxx = player.addItem(ItemProcessType.REWARD, r.getId(), r.getCount(), player, true);
								if (reward.getKey() == LuckyGameItemType.UNIQUE)
								{
									SystemMessage sm = new SystemMessage(this._type == LuckyGameType.LUXURY ? SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_X_S3_IN_THE_PREMIUM_LUCKY_GAME : SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_X_S3_IN_THE_STANDARD_LUCKY_GAME);
									sm.addPcName(player);
									sm.addLong(r.getCount());
									sm.addItemName(itemxx);
									player.broadcastPacket(sm);
									break;
								}
							}
						}

						player.sendItemList();
						player.getVariables().set("FortuneTelling", playCount >= 50 ? playCount - 50 : playCount);
						if (blackCat && playCount < 50)
						{
							player.getVariables().set("FortuneTellingBlackCat", true);
						}
					}
				}
			}
		}
	}
}
