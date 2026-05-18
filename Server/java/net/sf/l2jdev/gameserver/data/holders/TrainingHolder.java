package net.sf.l2jdev.gameserver.data.holders;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.gameserver.config.TrainingCampConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class TrainingHolder implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final int _objectId;
	private final int _classIndex;
	private final int _level;
	private final long _startTime;
	private long _endTime = -1L;
	private static final long TRAINING_DIVIDER = TimeUnit.SECONDS.toMinutes(TrainingCampConfig.TRAINING_CAMP_MAX_DURATION);

	public TrainingHolder(int objectId, int classIndex, int level, long startTime, long endTime)
	{
		this._objectId = objectId;
		this._classIndex = classIndex;
		this._level = level;
		this._startTime = startTime;
		this._endTime = endTime;
	}

	public long getEndTime()
	{
		return this._endTime;
	}

	public void setEndTime(long endTime)
	{
		this._endTime = endTime;
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public int getClassIndex()
	{
		return this._classIndex;
	}

	public int getLevel()
	{
		return this._level;
	}

	public long getStartTime()
	{
		return this._startTime;
	}

	public boolean isTraining()
	{
		return this._endTime == -1L;
	}

	public boolean isValid(Player player)
	{
		return TrainingCampConfig.TRAINING_CAMP_ENABLE && player.getObjectId() == this._objectId && player.getClassIndex() == this._classIndex;
	}

	public long getElapsedTime()
	{
		return TimeUnit.SECONDS.convert(System.currentTimeMillis() - this._startTime, TimeUnit.MILLISECONDS);
	}

	public long getRemainingTime()
	{
		return TimeUnit.SECONDS.toMinutes(TrainingCampConfig.TRAINING_CAMP_MAX_DURATION - this.getElapsedTime());
	}

	public long getTrainingTime(TimeUnit unit)
	{
		return Math.min(unit.convert(TrainingCampConfig.TRAINING_CAMP_MAX_DURATION, TimeUnit.SECONDS), unit.convert(this._endTime - this._startTime, TimeUnit.MILLISECONDS));
	}

	public static long getTrainingDivider()
	{
		return TRAINING_DIVIDER;
	}
}
