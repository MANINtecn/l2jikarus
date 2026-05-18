package net.sf.l2jdev.gameserver.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.Shutdown;
import net.sf.l2jdev.gameserver.config.ServerConfig;

public class ServerRestartManager
{
	static final Logger LOGGER = Logger.getLogger(ServerRestartManager.class.getName());
	private String nextRestartTime = "unknown";

	protected ServerRestartManager()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar restartTime = Calendar.getInstance();
			Calendar lastRestart = null;
			long delay = 0L;
			long lastDelay = 0L;

			for (String scheduledTime : ServerConfig.SERVER_RESTART_SCHEDULE)
			{
				String[] splitTime = scheduledTime.trim().split(":");
				restartTime.set(11, Integer.parseInt(splitTime[0]));
				restartTime.set(12, Integer.parseInt(splitTime[1]));
				restartTime.set(13, 0);
				if (restartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					restartTime.add(7, 1);
				}

				if (!ServerConfig.SERVER_RESTART_DAYS.isEmpty())
				{
					while (!ServerConfig.SERVER_RESTART_DAYS.contains(restartTime.get(7)))
					{
						restartTime.add(7, 1);
					}
				}

				delay = restartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				if (lastDelay == 0L)
				{
					lastDelay = delay;
					lastRestart = restartTime;
				}

				if (delay < lastDelay)
				{
					lastDelay = delay;
					lastRestart = restartTime;
				}
			}

			if (lastRestart != null)
			{
				if (!ServerConfig.SERVER_RESTART_DAYS.isEmpty() && ServerConfig.SERVER_RESTART_DAYS.size() != 7)
				{
					this.nextRestartTime = new SimpleDateFormat("MMMM d'" + this.getDayNumberSuffix(lastRestart.get(5)) + "' HH:mm", Locale.UK).format(lastRestart.getTime());
				}
				else
				{
					this.nextRestartTime = new SimpleDateFormat("HH:mm").format(lastRestart.getTime());
				}

				ThreadPool.schedule(new ServerRestartManager.ServerRestartTask(), lastDelay - ServerConfig.SERVER_RESTART_SCHEDULE_COUNTDOWN * 1000);
				LOGGER.info("Scheduled server restart at " + lastRestart.getTime() + ".");
			}
		}
		catch (Exception var13)
		{
			LOGGER.info("The scheduled server restart config is not set properly, please correct it!");
		}
	}

	public String getDayNumberSuffix(int day)
	{
		switch (day)
		{
			case 1:
			case 21:
			case 31:
				return "st";
			case 2:
			case 22:
				return "nd";
			case 3:
			case 23:
				return "rd";
			default:
				return "th";
		}
	}

	public String getNextRestartTime()
	{
		return this.nextRestartTime;
	}

	public static ServerRestartManager getInstance()
	{
		return ServerRestartManager.SingletonHolder.INSTANCE;
	}

	class ServerRestartTask implements Runnable
	{
		ServerRestartTask()
		{
			Objects.requireNonNull(ServerRestartManager.this);
			super();
		}

		@Override
		public void run()
		{
			Shutdown.getInstance().startShutdown(null, ServerConfig.SERVER_RESTART_SCHEDULE_COUNTDOWN, true);
		}
	}

	private static class SingletonHolder
	{
		protected static final ServerRestartManager INSTANCE = new ServerRestartManager();
	}
}
