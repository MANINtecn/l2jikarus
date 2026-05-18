package net.sf.l2jdev.gameserver.data.holders;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;

public class InstanceReenterTimeHolder
{
	private DayOfWeek _day = null;
	private int _hour = -1;
	private int _minute = -1;
	private long _time = -1L;

	public InstanceReenterTimeHolder(long time)
	{
		this._time = TimeUnit.MINUTES.toMillis(time);
	}

	public InstanceReenterTimeHolder(DayOfWeek day, int hour, int minute)
	{
		this._day = day;
		this._hour = hour;
		this._minute = minute;
	}

	public long getTime()
	{
		return this._time;
	}

	public DayOfWeek getDay()
	{
		return this._day;
	}

	public int getHour()
	{
		return this._hour;
	}

	public int getMinute()
	{
		return this._minute;
	}
}
