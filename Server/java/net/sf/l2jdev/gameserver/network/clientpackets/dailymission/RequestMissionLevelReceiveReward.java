package net.sf.l2jdev.gameserver.network.clientpackets.dailymission;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.MissionLevel;
import net.sf.l2jdev.gameserver.model.MissionLevelHolder;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MissionLevelPlayerDataHolder;
import net.sf.l2jdev.gameserver.model.actor.request.RewardRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.dailymission.ExMissionLevelRewardList;

public class RequestMissionLevelReceiveReward extends ClientPacket
{
	private final MissionLevelHolder _holder = MissionLevel.getInstance().getMissionBySeason(MissionLevel.getInstance().getCurrentSeason());
	private int _level;
	private int _rewardType;

	@Override
	protected void readImpl()
	{
		this._level = this.readInt();
		this._rewardType = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!player.hasRequest(RewardRequest.class))
			{
				player.addRequest(new RewardRequest(player));
				MissionLevelPlayerDataHolder info = player.getMissionLevelProgress();
				switch (this._rewardType)
				{
					case 1:
						if (!this._holder.getNormalRewards().containsKey(this._level) || info.getCollectedNormalRewards().contains(this._level) || info.getCurrentLevel() != this._level && info.getCurrentLevel() < this._level)
						{
							player.removeRequest(RewardRequest.class);
							return;
						}

						ItemHolder reward = this._holder.getNormalRewards().get(this._level);
						player.addItem(ItemProcessType.REWARD, reward.getId(), reward.getCount(), null, true);
						info.addToCollectedNormalRewards(this._level);
						info.storeInfoInVariable(player);
						break;
					case 2:
						if (!this._holder.getKeyRewards().containsKey(this._level) || info.getCollectedKeyRewards().contains(this._level) || info.getCurrentLevel() != this._level && info.getCurrentLevel() < this._level)
						{
							player.removeRequest(RewardRequest.class);
							return;
						}

						ItemHolder keyReward = this._holder.getKeyRewards().get(this._level);
						player.addItem(ItemProcessType.REWARD, keyReward.getId(), keyReward.getCount(), null, true);
						info.addToCollectedKeyReward(this._level);
						info.storeInfoInVariable(player);
						break;
					case 3:
						if (this._holder.getSpecialReward() == null || info.getCollectedSpecialReward() || info.getCurrentLevel() != this._level && info.getCurrentLevel() < this._level)
						{
							player.removeRequest(RewardRequest.class);
							return;
						}

						ItemHolder specialReward = this._holder.getSpecialReward();
						player.addItem(ItemProcessType.REWARD, specialReward.getId(), specialReward.getCount(), null, true);
						info.setCollectedSpecialReward(true);
						info.storeInfoInVariable(player);
						break;
					case 4:
						if (!this._holder.getBonusRewardIsAvailable() || this._holder.getBonusReward() == null || !info.getCollectedSpecialReward() || info.getCollectedBonusReward() || info.getCurrentLevel() != this._level && info.getCurrentLevel() < this._level)
						{
							player.removeRequest(RewardRequest.class);
							return;
						}

						if (!this._holder.getBonusRewardByLevelUp())
						{
							info.setCollectedBonusReward(true);
						}
						else
						{
							int maxNormalLevel = this._holder.getBonusLevel();
							int availableReward = -1;

							for (int level = maxNormalLevel; level <= this._holder.getMaxLevel(); level++)
							{
								if (level <= info.getCurrentLevel() && !info.getListOfCollectedBonusRewards().contains(level))
								{
									availableReward = level;
									break;
								}
							}

							if (availableReward == -1)
							{
								player.removeRequest(RewardRequest.class);
								return;
							}

							info.addToListOfCollectedBonusRewards(availableReward);
						}

						ItemHolder bonusReward = this._holder.getBonusReward();
						player.addItem(ItemProcessType.REWARD, bonusReward.getId(), bonusReward.getCount(), null, true);
						info.storeInfoInVariable(player);
				}

				player.sendPacket(new ExMissionLevelRewardList(player));
				ThreadPool.schedule(() -> player.removeRequest(RewardRequest.class), 300L);
			}
		}
	}
}
