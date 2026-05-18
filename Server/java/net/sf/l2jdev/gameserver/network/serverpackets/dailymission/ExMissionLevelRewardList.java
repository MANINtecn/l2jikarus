package net.sf.l2jdev.gameserver.network.serverpackets.dailymission;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.MissionLevel;
import net.sf.l2jdev.gameserver.model.MissionLevelHolder;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MissionLevelPlayerDataHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExMissionLevelRewardList extends ServerPacket
{
	private final int _year;
	private final int _month;
	private final int _maxNormalLevel;
	private final MissionLevelHolder _holder;
	private final MissionLevelPlayerDataHolder _info;
	private final List<Integer> _collectedNormalRewards;
	private final List<Integer> _collectedKeyRewards;
	private final List<Integer> _collectedBonusRewards;

	public ExMissionLevelRewardList(Player player)
	{
		MissionLevel missionData = MissionLevel.getInstance();
		int currentSeason = missionData.getCurrentSeason();
		String currentSeasonString = String.valueOf(currentSeason);
		this._year = Integer.parseInt(currentSeasonString.substring(0, 4));
		this._month = Integer.parseInt(currentSeasonString.substring(4, 6));
		this._holder = missionData.getMissionBySeason(currentSeason);
		this._maxNormalLevel = this._holder.getBonusLevel();
		this._info = player.getMissionLevelProgress();
		this._collectedNormalRewards = this._info.getCollectedNormalRewards();
		this._collectedKeyRewards = this._info.getCollectedKeyRewards();
		this._collectedBonusRewards = this._info.getListOfCollectedBonusRewards();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MISSION_LEVEL_REWARD_LIST.writeId(this, buffer);
		if (this._info.getCurrentLevel() == 0)
		{
			buffer.writeInt(1);
			buffer.writeInt(3);
			buffer.writeInt(-1);
			buffer.writeInt(0);
		}
		else
		{
			this.sendAvailableRewardsList(buffer, this._info);
		}

		buffer.writeInt(this._info.getCurrentLevel());
		buffer.writeInt(this.getPercent(this._info));
		buffer.writeInt(this._year);
		buffer.writeInt(this._month);
		buffer.writeInt(this.getAvailableRewards(this._info));
		if (this._holder.getBonusRewardIsAvailable() && this._holder.getBonusRewardByLevelUp())
		{
			boolean check = false;

			for (int level = this._maxNormalLevel; level <= this._holder.getMaxLevel(); level++)
			{
				if (level <= this._info.getCurrentLevel() && !this._collectedBonusRewards.contains(level))
				{
					check = true;
					break;
				}
			}

			buffer.writeInt(check);
		}
		else if (this._holder.getBonusRewardIsAvailable() && this._info.getCollectedSpecialReward() && !this._info.getCollectedBonusReward())
		{
			buffer.writeInt(1);
		}
		else
		{
			buffer.writeInt(0);
		}

		buffer.writeInt(0);
		buffer.writeByte(0);
	}

	private int getAvailableRewards(MissionLevelPlayerDataHolder info)
	{
		int availableRewards = 0;

		for (int level : this._holder.getNormalRewards().keySet())
		{
			if (level <= info.getCurrentLevel() && !this._collectedNormalRewards.contains(level))
			{
				availableRewards++;
			}
		}

		for (int levelx : this._holder.getKeyRewards().keySet())
		{
			if (levelx <= info.getCurrentLevel() && !this._collectedKeyRewards.contains(levelx))
			{
				availableRewards++;
			}
		}

		if (this._holder.getBonusRewardIsAvailable() && this._holder.getBonusRewardByLevelUp() && info.getCollectedSpecialReward())
		{
			List<Integer> collectedBonusRewards = info.getListOfCollectedBonusRewards();

			for (int levelxx = this._maxNormalLevel; levelxx <= this._holder.getMaxLevel(); levelxx++)
			{
				if (levelxx <= info.getCurrentLevel() && !collectedBonusRewards.contains(levelxx))
				{
					availableRewards++;
					break;
				}
			}
		}
		else if (this._holder.getBonusRewardIsAvailable() && this._holder.getBonusRewardByLevelUp() && info.getCurrentLevel() >= this._maxNormalLevel)
		{
			availableRewards++;
		}
		else if (this._holder.getBonusRewardIsAvailable() && info.getCurrentLevel() >= this._holder.getMaxLevel() && !info.getCollectedBonusReward() && info.getCollectedSpecialReward())
		{
			availableRewards++;
		}
		else if (info.getCurrentLevel() >= this._holder.getMaxLevel() && !info.getCollectedBonusReward())
		{
			availableRewards++;
		}

		return availableRewards;
	}

	private int getTotalRewards(MissionLevelPlayerDataHolder info)
	{
		int totalRewards = 0;

		for (int level : this._holder.getNormalRewards().keySet())
		{
			if (level <= info.getCurrentLevel())
			{
				totalRewards++;
			}
		}

		for (int levelx : this._holder.getKeyRewards().keySet())
		{
			if (levelx <= info.getCurrentLevel())
			{
				totalRewards++;
			}
		}

		if (this._holder.getBonusRewardByLevelUp() && info.getCollectedSpecialReward() && this._holder.getBonusRewardIsAvailable() && this._maxNormalLevel <= info.getCurrentLevel())
		{
			for (int levelxx = this._maxNormalLevel; levelxx <= this._holder.getMaxLevel(); levelxx++)
			{
				if (levelxx <= info.getCurrentLevel())
				{
					totalRewards++;
					break;
				}
			}
		}
		else if (info.getCollectedSpecialReward() && this._holder.getBonusRewardIsAvailable() && this._maxNormalLevel <= info.getCurrentLevel())
		{
			totalRewards++;
		}
		else if (this._maxNormalLevel <= info.getCurrentLevel())
		{
			totalRewards++;
		}

		return totalRewards;
	}

	private int getPercent(MissionLevelPlayerDataHolder info)
	{
		return info.getCurrentLevel() >= this._holder.getMaxLevel() ? 100 : (int) Math.floor((double) info.getCurrentEXP() / this._holder.getXPForSpecifiedLevel(info.getCurrentLevel()) * 100.0);
	}

	private void sendAvailableRewardsList(WritableBuffer buffer, MissionLevelPlayerDataHolder info)
	{
		buffer.writeInt(this.getTotalRewards(info));

		for (int level : this._holder.getNormalRewards().keySet())
		{
			if (level <= info.getCurrentLevel())
			{
				buffer.writeInt(1);
				buffer.writeInt(level);
				buffer.writeInt(this._collectedNormalRewards.contains(level) ? 2 : 1);
			}
		}

		for (int levelx : this._holder.getKeyRewards().keySet())
		{
			if (levelx <= info.getCurrentLevel())
			{
				buffer.writeInt(2);
				buffer.writeInt(levelx);
				buffer.writeInt(this._collectedKeyRewards.contains(levelx) ? 2 : 1);
			}
		}

		if (this._holder.getBonusRewardByLevelUp() && info.getCollectedSpecialReward() && this._holder.getBonusRewardIsAvailable() && this._maxNormalLevel <= info.getCurrentLevel())
		{
			buffer.writeInt(3);
			int sendLevel = 0;

			for (int levelxx = this._maxNormalLevel; levelxx <= this._holder.getMaxLevel(); levelxx++)
			{
				if (levelxx <= info.getCurrentLevel() && !this._collectedBonusRewards.contains(levelxx))
				{
					sendLevel = levelxx;
					break;
				}
			}

			buffer.writeInt(sendLevel == 0 ? this._holder.getMaxLevel() : sendLevel);
			buffer.writeInt(2);
		}
		else if (info.getCollectedSpecialReward() && this._holder.getBonusRewardIsAvailable() && this._maxNormalLevel <= info.getCurrentLevel())
		{
			buffer.writeInt(3);
			buffer.writeInt(this._holder.getMaxLevel());
			buffer.writeInt(2);
		}
		else if (this._maxNormalLevel <= info.getCurrentLevel())
		{
			buffer.writeInt(3);
			buffer.writeInt(this._holder.getMaxLevel());
			buffer.writeInt(!info.getCollectedSpecialReward());
		}
	}
}
