package net.sf.l2jdev.gameserver.network.clientpackets.huntpass;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.xml.HuntPassData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.HuntPass;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.RewardRequest;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassSayhasSupportInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassSimpleInfo;

public class RequestHuntPassRewardAll extends ClientPacket
{
	private int _huntPassType;

	@Override
	protected void readImpl()
	{
		this._huntPassType = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!OfflineTradeConfig.OFFLINE_DISCONNECT_SAME_ACCOUNT || !OfflinePlayConfig.OFFLINE_PLAY_DISCONNECT_SAME_ACCOUNT)
			{
				int sameAccountPlayers = 0;

				for (Player worldPlayer : World.getInstance().getPlayers())
				{
					if (worldPlayer.getAccountName().equals(player.getAccountName()))
					{
						sameAccountPlayers++;
					}
				}

				if (sameAccountPlayers > 1)
				{
					player.sendMessage("Hunting rewards are shared across the account and cannot be received if more than one character is online simultaneously.");
					return;
				}
			}

			if (!player.hasRequest(RewardRequest.class))
			{
				player.addRequest(new RewardRequest(player));
				boolean inventoryLimitReached = false;
				HuntPass huntPass = player.getHuntPass();

				while (true)
				{
					int rewardIndex = huntPass.getRewardStep();
					int premiumRewardIndex = huntPass.getPremiumRewardStep();
					if (rewardIndex >= HuntPassData.getInstance().getRewardsCount() && premiumRewardIndex >= HuntPassData.getInstance().getPremiumRewardsCount())
					{
						break;
					}

					ItemHolder reward = null;
					if (!huntPass.isPremium())
					{
						if (rewardIndex >= huntPass.getCurrentStep())
						{
							break;
						}

						reward = HuntPassData.getInstance().getRewards().get(rewardIndex);
					}
					else
					{
						if (premiumRewardIndex >= huntPass.getCurrentStep())
						{
							break;
						}

						if (rewardIndex < HuntPassData.getInstance().getRewardsCount())
						{
							reward = HuntPassData.getInstance().getRewards().get(rewardIndex);
						}
						else if (premiumRewardIndex < HuntPassData.getInstance().getPremiumRewardsCount())
						{
							reward = HuntPassData.getInstance().getPremiumRewards().get(premiumRewardIndex);
						}
					}

					if (reward == null)
					{
						break;
					}

					ItemTemplate itemTemplate = ItemData.getInstance().getTemplate(reward.getId());
					long weight = itemTemplate.getWeight() * reward.getCount();
					long slots = itemTemplate.isStackable() ? 1L : reward.getCount();
					if (!player.getInventory().validateWeight(weight) || !player.getInventory().validateCapacity(slots))
					{
						player.sendPacket(SystemMessageId.YOUR_INVENTORY_S_WEIGHT_SLOT_LIMIT_HAS_BEEN_EXCEEDED_SO_YOU_CANNOT_RECEIVE_THE_REWARD_PLEASE_FREE_UP_SOME_SPACE_AND_TRY_AGAIN);
						inventoryLimitReached = true;
						break;
					}

					this.normalReward(player);
					this.premiumReward(player);
					huntPass.setRewardStep(rewardIndex + 1);
				}

				if (!inventoryLimitReached)
				{
					huntPass.setRewardAlert(false);
				}

				huntPass.store();
				player.sendPacket(new HuntPassInfo(player, this._huntPassType));
				player.sendPacket(new HuntPassSayhasSupportInfo(player));
				player.sendPacket(new HuntPassSimpleInfo(player));
				ThreadPool.schedule(() -> player.removeRequest(RewardRequest.class), 300L);
			}
		}
	}

	protected void rewardItem(Player player, ItemHolder reward)
	{
		if (reward.getId() == 72286)
		{
			int count = (int) reward.getCount();
			player.getHuntPass().addSayhaTime(count);
			SystemMessage msg = new SystemMessage(SystemMessageId.YOU_VE_GOT_S1_SAYHA_S_GRACE_SUSTENTION_POINT_S);
			msg.addInt(count);
			player.sendPacket(msg);
		}
		else
		{
			player.addItem(ItemProcessType.REWARD, reward, player, true);
		}
	}

	private void premiumReward(Player player)
	{
		HuntPass huntPass = player.getHuntPass();
		int premiumRewardIndex = huntPass.getPremiumRewardStep();
		if (premiumRewardIndex < HuntPassData.getInstance().getPremiumRewardsCount())
		{
			if (huntPass.isPremium())
			{
				this.rewardItem(player, HuntPassData.getInstance().getPremiumRewards().get(premiumRewardIndex));
				huntPass.setPremiumRewardStep(premiumRewardIndex + 1);
			}
		}
	}

	private void normalReward(Player player)
	{
		HuntPass huntPass = player.getHuntPass();
		int rewardIndex = huntPass.getRewardStep();
		if (rewardIndex < HuntPassData.getInstance().getRewardsCount())
		{
			if (!huntPass.isPremium() || huntPass.getPremiumRewardStep() >= rewardIndex && huntPass.getPremiumRewardStep() < HuntPassData.getInstance().getPremiumRewardsCount())
			{
				this.rewardItem(player, HuntPassData.getInstance().getRewards().get(rewardIndex));
			}
		}
	}
}
