package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class PlayerRelicData
{
	private final int _relicId;
	private int _relicLevel;
	private int _relicCount;
	private int _relicIndex;
	private long _relicSummonTime;

	public PlayerRelicData(int relicId, int relicLevel, int relicCount, int relicIndex, long relicSummonTime)
	{
		this._relicId = relicId;
		this._relicLevel = relicLevel;
		this._relicCount = relicCount;
		this._relicIndex = relicIndex;
		this._relicSummonTime = relicSummonTime;
	}

	public int getRelicId()
	{
		return this._relicId;
	}

	public int getRelicLevel()
	{
		return this._relicLevel;
	}

	public int getRelicCount()
	{
		return this._relicCount;
	}

	public int getRelicIndex()
	{
		return this._relicIndex;
	}

	public long getRelicSummonTime()
	{
		return this._relicSummonTime;
	}

	public void setRelicLevel(int level)
	{
		this._relicLevel = level;
	}

	public void setRelicCount(int count)
	{
		this._relicCount = count;
	}

	public void setRelicIndex(int index)
	{
		this._relicIndex = index;
	}

	public void setRelicSummonTime(long relicSummonTime)
	{
		this._relicSummonTime = relicSummonTime;
	}
}
