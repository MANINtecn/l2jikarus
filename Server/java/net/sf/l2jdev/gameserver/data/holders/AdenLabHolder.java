package net.sf.l2jdev.gameserver.data.holders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.data.enums.AdenLabGameType;

public class AdenLabHolder
{
	private AdenLabGameType _gameType;
	private byte _bossId;
	private byte _pageIndex;
	private byte _cardCount;
	private float _gameSuccessRate;
	private final Map<Byte, Map<Byte, List<AdenLabStageHolder>>> _options = new HashMap<>();

	public void setBossId(byte bossId)
	{
		this._bossId = bossId;
	}

	public void setGameType(AdenLabGameType gameType)
	{
		this._gameType = gameType;
	}

	public void setPageIndex(byte pageIndex)
	{
		this._pageIndex = pageIndex < 0 ? 0 : pageIndex;
	}

	public void setGameSuccessRate(float value)
	{
		this._gameSuccessRate = value < 0.0F ? 0.0F : value;
	}

	public void setCardCount(byte cardCount)
	{
		this._cardCount = cardCount < 0 ? 0 : cardCount;
	}

	public void addStage(byte optionIndex, byte stageLevel, AdenLabStageHolder stageHolder)
	{
		Map<Byte, List<AdenLabStageHolder>> levelMap = this._options.computeIfAbsent(optionIndex, _ -> new HashMap<>());
		levelMap.computeIfAbsent(stageLevel, _ -> new ArrayList<>()).add(stageHolder);
	}

	public void addStages(byte optionIndex, byte stageLevel, List<AdenLabStageHolder> stageMap)
	{
		Map<Byte, List<AdenLabStageHolder>> levelMap = this._options.computeIfAbsent(optionIndex, _ -> new HashMap<>());
		levelMap.computeIfAbsent(stageLevel, _ -> new ArrayList<>()).addAll(stageMap);
	}

	public byte getBossId()
	{
		return this._bossId;
	}

	public AdenLabGameType getGameType()
	{
		return this._gameType;
	}

	public int getPageIndex()
	{
		return this._pageIndex;
	}

	public float getGameSuccessRate()
	{
		return this._gameSuccessRate;
	}

	public byte getCardCount()
	{
		return this._cardCount;
	}

	public Map<Byte, Map<Byte, List<AdenLabStageHolder>>> getOptions()
	{
		return this._options;
	}

	public List<AdenLabStageHolder> getStageHolderListByLevel(byte optionIndex, int stageLevel)
	{
		Map<Byte, List<AdenLabStageHolder>> stageMap = this._options.computeIfAbsent(optionIndex, _ -> new HashMap<>());
		return stageMap.computeIfAbsent((byte) stageLevel, _ -> new ArrayList<>());
	}
}
