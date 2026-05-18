package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Calendar;

import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.OnDayNightChange;

public class GameTimeTaskManager extends Thread
{
	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 100;
	public static final int IG_DAYS_PER_DAY = 6;
	public static final int MILLIS_PER_IG_DAY = 14400000;
	public static final int SECONDS_PER_IG_DAY = 14400;
	public static final int TICKS_PER_IG_DAY = 144000;
	private final long _referenceTime;
	private boolean _isNight;
	private int _gameTicks;
	private int _gameTime;
	private int _gameHour;

	protected GameTimeTaskManager()
	{
		super("GameTimeTaskManager");
		super.setDaemon(true);
		super.setPriority(10);
		Calendar c = Calendar.getInstance();
		c.set(11, 0);
		c.set(12, 0);
		c.set(13, 0);
		c.set(14, 0);
		this._referenceTime = c.getTimeInMillis();
		super.start();
	}

	@Override
	public void run()
	{
		while (true)
		{
			this._gameTicks = (int) ((System.currentTimeMillis() - this._referenceTime) / 100L);
			this._gameTime = this._gameTicks % 144000 / 100;
			this._gameHour = this._gameTime / 60;
			if (this._gameHour < 6 != this._isNight)
			{
				this._isNight = !this._isNight;
				if (EventDispatcher.getInstance().hasListener(EventType.ON_DAY_NIGHT_CHANGE))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnDayNightChange(this._isNight));
				}
			}

			try
			{
				Thread.sleep(100L);
			}
			catch (InterruptedException var2)
			{
			}
		}
	}

	public boolean isNight()
	{
		return this._isNight;
	}

	public int getGameTicks()
	{
		return this._gameTicks;
	}

	public int getGameTime()
	{
		return this._gameTime;
	}

	public int getGameHour()
	{
		return this._gameHour;
	}

	public int getGameMinute()
	{
		return this._gameTime % 60;
	}

	public static final GameTimeTaskManager getInstance()
	{
		return GameTimeTaskManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final GameTimeTaskManager INSTANCE = new GameTimeTaskManager();
	}
}
