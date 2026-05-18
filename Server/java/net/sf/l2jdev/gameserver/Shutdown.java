package net.sf.l2jdev.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.config.DatabaseConfig;
import net.sf.l2jdev.commons.database.DatabaseBackup;
import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PrisonConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.BotReportTable;
import net.sf.l2jdev.gameserver.data.SchemeBufferTable;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.sql.OfflinePlayTable;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.data.sql.PartyMatchingHistoryTable;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.managers.GrandBossManager;
import net.sf.l2jdev.gameserver.managers.ItemAuctionManager;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.managers.PrecautionaryRestartManager;
import net.sf.l2jdev.gameserver.managers.RevengeHistoryManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.managers.events.BlackCouponManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.prison.PrisonManager;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerClose;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class Shutdown extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(Shutdown.class.getName());
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT = new String[]
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting"
	};
	private static Shutdown _counterInstance;
	private static boolean _countdownFinished;
	private static volatile int _secondsShut;
	private static volatile int _shutdownMode;

	private static void sendServerQuit(int seconds)
	{
		SystemMessage sysm = new SystemMessage(SystemMessageId.THE_SERVER_WILL_BE_SHUT_DOWN_IN_S1_SEC_PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT);
		sysm.addInt(seconds);
		Broadcast.toAllOnlinePlayers(sysm);
	}

	protected Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = 0;
	}

	public Shutdown(int seconds, boolean restart)
	{
		_secondsShut = Math.max(0, seconds);
		_shutdownMode = restart ? 2 : 1;
	}

	@Override
	public void run()
	{
		if (this == getInstance())
		{
			startShutdownActions();
		}
		else if (!_countdownFinished)
		{
			countdown();
			LOGGER.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch (_shutdownMode)
			{
				case 1:
					setMode(1);
					startShutdownActions();
					System.exit(0);
					break;
				case 2:
					setMode(2);
					startShutdownActions();
					System.exit(2);
					break;
				case 3:
					LoginServerThread.getInstance().setServerStatus(0);
			}
		}
	}

	public void startShutdown(Player player, int seconds, boolean restart)
	{
		_shutdownMode = restart ? 2 : 1;
		if (player != null)
		{
			LOGGER.warning("GM: " + player.getName() + "(" + player.getObjectId() + ") issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		}
		else
		{
			LOGGER.warning("Server scheduled restart issued shutdown command. " + (restart ? "Restart" : "Shutdown") + " in " + seconds + " seconds!");
		}

		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 10:
				case 30:
				case 60:
				case 120:
				case 180:
				case 240:
				case 300:
				case 360:
				case 420:
				case 480:
				case 540:
					break;
				default:
					sendServerQuit(seconds);
			}
		}

		if (_counterInstance != null)
		{
			Shutdown.abort();
		}

		if (ServerConfig.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance().restartEnabled();
		}

		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}

	public void abort(Player player)
	{
		if (_countdownFinished)
		{
			LOGGER.warning("GM: " + (player != null ? player.getName() + "(" + player.getObjectId() + ") " : "") + "shutdown ABORT failed because countdown has finished.");
		}
		else
		{
			LOGGER.warning("GM: " + (player != null ? player.getName() + "(" + player.getObjectId() + ") " : "") + "issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
			if (_counterInstance != null)
			{
				Shutdown.abort();
				if (ServerConfig.PRECAUTIONARY_RESTART_ENABLED)
				{
					PrecautionaryRestartManager.getInstance().restartAborted();
				}

				Broadcast.toAllOnlinePlayers("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!", false);
			}
		}
	}

	private static void setMode(int mode)
	{
		_shutdownMode = mode;
	}

	private static void abort()
	{
		_shutdownMode = 3;
	}

	private static void countdown()
	{
		try
		{
			while (_secondsShut > 0)
			{
				if (_shutdownMode != 3)
				{
					switch (_secondsShut)
					{
						case 1:
						case 2:
						case 3:
						case 4:
						case 5:
						case 10:
						case 30:
						case 60:
						case 120:
						case 180:
						case 240:
						case 300:
						case 360:
						case 420:
						case 480:
						case 540:
							sendServerQuit(_secondsShut);
						default:
							if (_secondsShut <= 60 && LoginServerThread.getInstance().getServerStatus() != 4)
							{
								LoginServerThread.getInstance().setServerStatus(4);
							}

							_secondsShut--;
							Thread.sleep(1000L);
					}
				}
				else
				{
					if (LoginServerThread.getInstance().getServerStatus() == 4)
					{
						LoginServerThread.getInstance().setServerStatus(GeneralConfig.SERVER_GMONLY ? 5 : 0);
					}
					break;
				}
			}
		}
		catch (Exception var2)
		{
		}
	}

	private static void startShutdownActions()
	{
		if (!_countdownFinished)
		{
			_countdownFinished = true;
			Shutdown.TimeCounter tc = new Shutdown.TimeCounter();
			Shutdown.TimeCounter tc1 = new Shutdown.TimeCounter();

			try
			{
				if ((OfflineTradeConfig.OFFLINE_TRADE_ENABLE || OfflineTradeConfig.OFFLINE_CRAFT_ENABLE) && OfflineTradeConfig.RESTORE_OFFLINERS && !OfflineTradeConfig.STORE_OFFLINE_TRADE_IN_REALTIME)
				{
					OfflineTraderTable.getInstance().storeOffliners();
					LOGGER.info("Offline Traders Table: Offline shops stored(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
				}
			}
			catch (Throwable var11)
			{
				LOGGER.log(Level.WARNING, "Error saving offline shops.", var11);
			}

			try
			{
				if (OfflinePlayConfig.RESTORE_AUTO_PLAY_OFFLINERS && GeneralConfig.ENABLE_AUTO_ASSIST)
				{
					OfflinePlayTable.getInstance().storeOfflineGroups();
					LOGGER.info("Offline Play Table: Offline play groups stored(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
				}
			}
			catch (Throwable var10)
			{
				LOGGER.log(Level.WARNING, "Error saving offline play groups.", var10);
			}

			try
			{
				disconnectAllCharacters();
				LOGGER.info("All players disconnected and saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable var9)
			{
			}

			try
			{
				BlackCouponManager.getInstance().storeMe();
				LOGGER.info("Black Coupon Manager: Destroyed items saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable var8)
			{
			}

			try
			{
				GameTimeTaskManager.getInstance().interrupt();
				LOGGER.info("Game Time Task Manager: Thread interruped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable var7)
			{
			}

			try
			{
				ThreadPool.shutdown();
				LOGGER.info("Thread Pool Manager: Manager has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable var6)
			{
			}

			try
			{
				LoginServerThread.getInstance().interrupt();
				LOGGER.info("Login Server Thread: Thread interruped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable var5)
			{
			}

			saveData();
			tc.restartCounter();

			try
			{
				DatabaseFactory.close();
				LOGGER.info("Database Factory: Database connection has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable var4)
			{
			}

			if (DatabaseConfig.BACKUP_DATABASE)
			{
				DatabaseBackup.performBackup("game");
			}

			LOGGER.info("The server has been successfully shut down in " + tc1.getEstimatedTime() / 1000L + "seconds.");
		}
	}

	private static void saveData()
	{
		switch (_shutdownMode)
		{
			case 0:
				LOGGER.info("SIGTERM received. Shutting down NOW!");
				break;
			case 1:
				LOGGER.info("GM shutdown received. Shutting down NOW!");
				break;
			case 2:
				LOGGER.info("GM restart received. Restarting NOW!");
		}

		Shutdown.TimeCounter tc = new Shutdown.TimeCounter();
		DatabaseSpawnManager.getInstance().cleanUp();
		LOGGER.info("RaidBossSpawnManager: All raidboss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		GrandBossManager.getInstance().cleanUp();
		LOGGER.info("GrandBossManager: All Grand Boss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		ItemAuctionManager.getInstance().shutdown();
		LOGGER.info("Item Auction Manager: All tasks stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		Olympiad.getInstance().saveOlympiadStatus();
		LOGGER.info("Olympiad System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		Hero.getInstance().shutdown();
		LOGGER.info("Hero System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		ClanTable.getInstance().shutdown();
		LOGGER.info("Clan System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		RevengeHistoryManager.getInstance().storeMe();
		LOGGER.info("Revenge History Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		CursedWeaponsManager.getInstance().saveData();
		LOGGER.info("Cursed Weapons Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		if (!GeneralConfig.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManorManager.getInstance().storeMe();
			LOGGER.info("Castle Manor Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		}

		ScriptManager.getInstance().save();
		LOGGER.info("Script Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		GlobalVariablesManager.getInstance().storeMe();
		LOGGER.info("Global Variables Manager: Variables saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		PartyMatchingHistoryTable.getInstance().storeMe();
		LOGGER.info("Party Matching History Table: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		SchemeBufferTable.getInstance().saveSchemes();
		LOGGER.info("SchemeBufferTable data has been saved.");
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			WorldExchangeManager.getInstance().storeMe();
			LOGGER.info("World Exchange Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		}

		if (GeneralConfig.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			LOGGER.info("Items On Ground Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			ItemsOnGroundManager.getInstance().cleanUp();
			LOGGER.info("Items On Ground Manager: Cleaned up(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		}

		if (GeneralConfig.BOTREPORT_ENABLE)
		{
			BotReportTable.getInstance().saveReportedCharData();
			LOGGER.info("Bot Report Table: Successfully saved reports to database!");
		}

		if (PrisonConfig.ENABLE_PRISON)
		{
			PrisonManager.savePrisoners();
		}

		try
		{
			Thread.sleep(5000L);
		}
		catch (Exception var3)
		{
		}
	}

	private static void disconnectAllCharacters()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			Disconnection.of(player).storeAndDeleteWith(ServerClose.STATIC_PACKET);
		}
	}

	public static Shutdown getInstance()
	{
		return Shutdown.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final Shutdown INSTANCE = new Shutdown();
	}

	private static class TimeCounter
	{
		private long _startTime;

		protected TimeCounter()
		{
			this.restartCounter();
		}

		public void restartCounter()
		{
			this._startTime = System.currentTimeMillis();
		}

		public long getEstimatedTimeAndRestartCounter()
		{
			long toReturn = System.currentTimeMillis() - this._startTime;
			this.restartCounter();
			return toReturn;
		}

		public long getEstimatedTime()
		{
			return System.currentTimeMillis() - this._startTime;
		}
	}
}
