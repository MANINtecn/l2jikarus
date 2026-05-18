package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.data.xml.MissionLevel;
import net.sf.l2jdev.gameserver.model.MissionLevelHolder;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class MissionLevelPlayerDataHolder
{
	private int _currentLevel = 0;
	private int _currentEXP = 0;
	private final List<Integer> _collectedNormalRewards = new ArrayList<>();
	private final List<Integer> _collectedKeyRewards = new ArrayList<>();
	private boolean _collectedSpecialReward = false;
	private boolean _collectedBonusReward = false;
	private final List<Integer> _listOfCollectedBonusRewards = new ArrayList<>();

	public MissionLevelPlayerDataHolder()
	{
	}

	public MissionLevelPlayerDataHolder(String variable)
	{
		for (String data : variable.split(";"))
		{
			List<String> values = new ArrayList<>(List.of(data.split(":")));
			String key = values.get(0);
			values.remove(0);
			if (key.equals("CurrentLevel"))
			{
				this._currentLevel = Integer.parseInt(values.get(0));
			}
			else if (key.equals("LevelXP"))
			{
				this._currentEXP = Integer.parseInt(values.get(0));
			}
			else if (key.equals("SpecialReward"))
			{
				this._collectedSpecialReward = Boolean.parseBoolean(values.get(0));
			}
			else if (key.equals("BonusReward"))
			{
				this._collectedBonusReward = Boolean.parseBoolean(values.get(0));
				if (this._collectedBonusReward && MissionLevel.getInstance().getMissionBySeason(MissionLevel.getInstance().getCurrentSeason()).getBonusRewardByLevelUp())
				{
					this._collectedBonusReward = false;
				}
			}
			else
			{
				List<Integer> valuesData = new ArrayList<>();
				String[] missions = values.isEmpty() ? values.toArray(new String[0]) : values.get(0).split(",");

				for (String mission : missions)
				{
					valuesData.add(Integer.parseInt(mission));
				}

				if (key.equals("ListOfNormalRewards"))
				{
					this._collectedNormalRewards.addAll(valuesData);
				}
				else if (key.equals("ListOfKeyRewards"))
				{
					this._collectedKeyRewards.addAll(valuesData);
				}
				else if (key.equals("ListOfBonusRewards"))
				{
					this._listOfCollectedBonusRewards.addAll(valuesData);
					if (!this._collectedBonusReward && !this._listOfCollectedBonusRewards.isEmpty() && !MissionLevel.getInstance().getMissionBySeason(MissionLevel.getInstance().getCurrentSeason()).getBonusRewardByLevelUp())
					{
						this._collectedBonusReward = true;
					}
				}
			}
		}
	}

	public String getVariablesFromInfo()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CurrentLevel").append(":").append(this._currentLevel).append(";");
		sb.append("LevelXP").append(":").append(this._currentEXP).append(";");
		sb.append("ListOfNormalRewards").append(":");
		sb.append(this.getStringFromList(this._collectedNormalRewards));
		sb.append(";");
		sb.append("ListOfKeyRewards").append(":");
		sb.append(this.getStringFromList(this._collectedKeyRewards));
		sb.append(";");
		sb.append("SpecialReward").append(":");
		sb.append(this._collectedSpecialReward);
		sb.append(";");
		sb.append("BonusReward").append(":");
		sb.append(this._collectedBonusReward);
		sb.append(";");
		sb.append("ListOfBonusRewards").append(":");
		sb.append(this.getStringFromList(this._listOfCollectedBonusRewards));
		sb.append(";");
		return sb.toString();
	}

	public String getStringFromList(List<Integer> list)
	{
		StringBuilder sb = new StringBuilder();

		for (int value : list)
		{
			sb.append(value);
			if (list.lastIndexOf(value) == list.size() - 1)
			{
				break;
			}

			sb.append(",");
		}

		return sb.toString();
	}

	public void storeInfoInVariable(Player player)
	{
		player.getVariables().set("MISSION_LEVEL_PROGRESS_" + MissionLevel.getInstance().getCurrentSeason(), this.getVariablesFromInfo());
	}

	public void calculateEXP(int exp)
	{
		MissionLevelHolder holder = MissionLevel.getInstance().getMissionBySeason(MissionLevel.getInstance().getCurrentSeason());
		if (this.getCurrentLevel() < holder.getMaxLevel())
		{
			int giveEXP = exp;

			while (true)
			{
				try
				{
					int takeEXP = holder.getXPForSpecifiedLevel(this.getCurrentLevel() + 1) - (this.getCurrentEXP() + giveEXP);
					if (takeEXP > 0)
					{
						this.setCurrentEXP(this.getCurrentEXP() + giveEXP);
						break;
					}

					giveEXP = Math.abs(takeEXP);
					this.setCurrentLevel(this.getCurrentLevel() + 1);
					this.setCurrentEXP(0);
				}
				catch (NullPointerException var5)
				{
					break;
				}
			}
		}
	}

	public int getCurrentLevel()
	{
		return this._currentLevel;
	}

	public void setCurrentLevel(int currentLevel)
	{
		this._currentLevel = currentLevel;
	}

	public int getCurrentEXP()
	{
		return this._currentEXP;
	}

	public void setCurrentEXP(int currentEXP)
	{
		this._currentEXP = currentEXP;
	}

	public List<Integer> getCollectedNormalRewards()
	{
		return this._collectedNormalRewards;
	}

	public void addToCollectedNormalRewards(int pos)
	{
		this._collectedNormalRewards.add(pos);
	}

	public List<Integer> getCollectedKeyRewards()
	{
		return this._collectedKeyRewards;
	}

	public void addToCollectedKeyReward(int pos)
	{
		this._collectedKeyRewards.add(pos);
	}

	public boolean getCollectedSpecialReward()
	{
		return this._collectedSpecialReward;
	}

	public void setCollectedSpecialReward(boolean collectedSpecialReward)
	{
		this._collectedSpecialReward = collectedSpecialReward;
	}

	public boolean getCollectedBonusReward()
	{
		return this._collectedBonusReward;
	}

	public void setCollectedBonusReward(boolean collectedBonusReward)
	{
		this._collectedBonusReward = collectedBonusReward;
	}

	public List<Integer> getListOfCollectedBonusRewards()
	{
		return this._listOfCollectedBonusRewards;
	}

	public void addToListOfCollectedBonusRewards(int pos)
	{
		this._listOfCollectedBonusRewards.add(pos);
	}
}
