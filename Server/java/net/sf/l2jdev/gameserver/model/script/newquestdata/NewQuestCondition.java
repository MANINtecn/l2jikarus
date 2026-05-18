package net.sf.l2jdev.gameserver.model.script.newquestdata;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;

public class NewQuestCondition
{
	private final int _minLevel;
	private final int _maxLevel;
	private final List<Integer> _previousQuestIds;
	private final List<PlayerClass> _allowedClassIds;
	private final boolean _oneOfPreQuests;
	private final boolean _specificStart;

	public NewQuestCondition(int minLevel, int maxLevel, List<Integer> previousQuestIds, List<PlayerClass> allowedClassIds, boolean oneOfPreQuests, boolean specificStart)
	{
		this._minLevel = minLevel;
		this._maxLevel = maxLevel;
		this._previousQuestIds = previousQuestIds;
		this._allowedClassIds = allowedClassIds;
		this._oneOfPreQuests = oneOfPreQuests;
		this._specificStart = specificStart;
	}

	public int getMinLevel()
	{
		return this._minLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public List<Integer> getPreviousQuestIds()
	{
		return this._previousQuestIds;
	}

	public List<PlayerClass> getAllowedClassIds()
	{
		return this._allowedClassIds;
	}

	public boolean getOneOfPreQuests()
	{
		return this._oneOfPreQuests;
	}

	public boolean getSpecificStart()
	{
		return this._specificStart;
	}
}
