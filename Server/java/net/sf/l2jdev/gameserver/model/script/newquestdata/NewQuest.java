package net.sf.l2jdev.gameserver.model.script.newquestdata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class NewQuest
{
	private static final Logger LOGGER = Logger.getLogger(NewQuest.class.getName());
	private final int _id;
	private int _questType;
	private final String _name;
	private final int _startNpcId;
	private final int _endNpcId;
	private final int _startItemId;
	private final NewQuestLocation _location;
	private final NewQuestCondition _conditions;
	private final NewQuestGoal _goal;
	private final NewQuestReward _rewards;

	public NewQuest(StatSet set)
	{
		this._id = set.getInt("id", -1);
		this._questType = set.getInt("type", -1);
		this._name = set.getString("name", "");
		this._startNpcId = set.getInt("startNpcId", -1);
		this._endNpcId = set.getInt("endNpcId", -1);
		this._startItemId = set.getInt("startItemId", -1);
		this._location = new NewQuestLocation(set.getInt("startLocationId", 0), set.getInt("endLocationId", 0), set.getInt("questLocationId", 0));
		String classIds = set.getString("classIds", "");
		List<PlayerClass> classRestriction = classIds.isEmpty() ? Collections.emptyList() : Arrays.stream(classIds.split(";")).map(it -> PlayerClass.getPlayerClass(Integer.parseInt(it))).collect(Collectors.toList());
		String preQuestId = set.getString("preQuestId", "");
		List<Integer> preQuestIds = preQuestId.isEmpty() ? Collections.emptyList() : Arrays.stream(preQuestId.split(";")).map(it -> Integer.parseInt(it)).collect(Collectors.toList());
		this._conditions = new NewQuestCondition(set.getInt("minLevel", -1), set.getInt("maxLevel", ExperienceData.getInstance().getMaxLevel()), preQuestIds, classRestriction, set.getBoolean("oneOfPreQuests", false), set.getBoolean("specificStart", false));
		int goalItemId = set.getInt("goalItemId", -1);
		int goalCount = set.getInt("goalCount", -1);
		if (goalItemId > 0)
		{
			ItemTemplate template = ItemData.getInstance().getTemplate(goalItemId);
			if (template == null)
			{
				LOGGER.warning(this.getClass().getSimpleName() + this._id + ": Could not find goal item template with id " + goalItemId);
			}
			else if (goalCount > 1)
			{
				if (!template.isStackable())
				{
					LOGGER.warning(this.getClass().getSimpleName() + this._id + ": Item template with id " + goalItemId + " should be stackable.");
				}

				if (!template.isQuestItem())
				{
					LOGGER.warning(this.getClass().getSimpleName() + this._id + ": Item template with id " + goalItemId + " should be quest item.");
				}
			}
		}

		this._goal = new NewQuestGoal(goalItemId, goalCount, set.getString("goalString", ""));
		this._rewards = new NewQuestReward(set.getLong("rewardExp", -1L), set.getLong("rewardSp", -1L), set.getInt("rewardLevel", -1), set.getList("rewardItems", ItemHolder.class));
	}

	public int getId()
	{
		return this._id;
	}

	public int getQuestType()
	{
		return this._questType;
	}

	public String getName()
	{
		return this._name;
	}

	public int getStartNpcId()
	{
		return this._startNpcId;
	}

	public int getEndNpcId()
	{
		return this._endNpcId;
	}

	public int getStartItemId()
	{
		return this._startItemId;
	}

	public NewQuestLocation getLocation()
	{
		return this._location;
	}

	public NewQuestCondition getConditions()
	{
		return this._conditions;
	}

	public NewQuestGoal getGoal()
	{
		return this._goal;
	}

	public NewQuestReward getRewards()
	{
		return this._rewards;
	}

	public void setType(int type)
	{
		this._questType = type;
	}
}
