package net.sf.l2jdev.gameserver.model.stats;

import java.time.Duration;
import java.time.Instant;

public class BasicPropertyResist
{
	private static final Duration RESIST_DURATION = Duration.ofSeconds(15L);
	private volatile Instant _resistanceEndTime = Instant.MIN;
	private volatile int _resistanceLevel;

	public boolean isExpired()
	{
		return Instant.now().isAfter(this._resistanceEndTime);
	}

	public Duration getRemainTime()
	{
		return Duration.between(Instant.now(), this._resistanceEndTime);
	}

	public int getResistLevel()
	{
		return !this.isExpired() ? this._resistanceLevel : 0;
	}

	public synchronized void increaseResistLevel()
	{
		if (this.isExpired())
		{
			this._resistanceLevel = 1;
			this._resistanceEndTime = Instant.now().plus(RESIST_DURATION);
		}
		else
		{
			this._resistanceLevel++;
		}
	}
}
