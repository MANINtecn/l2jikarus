package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.model.actor.enums.player.DailyMissionStatus;

public class DailyMissionPlayerEntry
{
	private final int _objectId;
	private final int _rewardId;
	private DailyMissionStatus _status = DailyMissionStatus.NOT_AVAILABLE;
	private int _progress;
	private long _lastCompleted;
	private boolean _recentlyCompleted;

	public DailyMissionPlayerEntry(int objectId, int rewardId)
	{
		this._objectId = objectId;
		this._rewardId = rewardId;
	}

	public DailyMissionPlayerEntry(int objectId, int rewardId, int status, int progress, long lastCompleted)
	{
		this(objectId, rewardId);
		this._status = DailyMissionStatus.valueOf(status);
		this._progress = progress;
		this._lastCompleted = lastCompleted;
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public int getRewardId()
	{
		return this._rewardId;
	}

	public DailyMissionStatus getStatus()
	{
		return this._status;
	}

	public void setStatus(DailyMissionStatus status)
	{
		this._status = status;
	}

	public int getProgress()
	{
		return this._progress;
	}

	public void setProgress(int progress)
	{
		this._progress = progress;
	}

	public int increaseProgress()
	{
		this._progress++;
		return this._progress;
	}

	public long getLastCompleted()
	{
		return this._lastCompleted;
	}

	public void setLastCompleted(long lastCompleted)
	{
		this._lastCompleted = lastCompleted;
	}

	public boolean isRecentlyCompleted()
	{
		return this._recentlyCompleted;
	}

	public void setRecentlyCompleted(boolean recentlyCompleted)
	{
		this._recentlyCompleted = recentlyCompleted;
	}
}
