package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogout;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;

public class PremiumManager
{
	private static final Logger LOGGER = Logger.getLogger(PremiumManager.class.getName());
	public static final String LOAD_SQL = "SELECT account_name,enddate FROM account_premium WHERE account_name = ?";
	public static final String UPDATE_SQL = "REPLACE INTO account_premium (account_name,enddate) VALUE (?,?)";
	public static final String DELETE_SQL = "DELETE FROM account_premium WHERE account_name = ?";
	private final Map<String, Long> _premiumData = new ConcurrentHashMap<>();
	private final Map<String, ScheduledFuture<?>> _expiretasks = new ConcurrentHashMap<>();
	private final ListenersContainer _listenerContainer = Containers.Players();
	private final Consumer<OnPlayerLogin> _playerLoginEvent = event -> {
		Player player = event.getPlayer();
		String accountName = player.getAccountName();
		this.loadPremiumData(accountName);
		long now = System.currentTimeMillis();
		long premiumExpiration = this.getPremiumExpiration(accountName);
		player.setPremiumStatus(premiumExpiration > now);
		if (player.hasPremiumStatus())
		{
			this.startExpireTask(player, premiumExpiration - now);
		}
		else if (premiumExpiration > 0L)
		{
			this.removePremiumStatus(accountName, false);
		}
	};
	private final Consumer<OnPlayerLogout> _playerLogoutEvent = event -> this.stopExpireTask(event.getPlayer());

	protected PremiumManager()
	{
		this._listenerContainer.addListener(new ConsumerEventListener(this._listenerContainer, EventType.ON_PLAYER_LOGIN, event -> this._playerLoginEvent.accept((OnPlayerLogin) event), this));
		this._listenerContainer.addListener(new ConsumerEventListener(this._listenerContainer, EventType.ON_PLAYER_LOGOUT, event -> this._playerLogoutEvent.accept((OnPlayerLogout) event), this));
	}

	private void startExpireTask(Player player, long delay)
	{
		this._expiretasks.put(player.getAccountName().toLowerCase(), ThreadPool.schedule(new PremiumManager.PremiumExpireTask(player), delay));
	}

	private void stopExpireTask(Player player)
	{
		ScheduledFuture<?> task = this._expiretasks.remove(player.getAccountName().toLowerCase());
		if (task != null)
		{
			task.cancel(false);
		}
	}

	private void loadPremiumData(String accountName)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT account_name,enddate FROM account_premium WHERE account_name = ?");)
		{
			stmt.setString(1, accountName.toLowerCase());

			try (ResultSet rset = stmt.executeQuery())
			{
				while (rset.next())
				{
					this._premiumData.put(rset.getString(1).toLowerCase(), rset.getLong(2));
				}
			}
		}
		catch (SQLException var13)
		{
			LOGGER.warning("Problem with PremiumManager: " + var13.getMessage());
		}
	}

	public long getPremiumExpiration(String accountName)
	{
		return this._premiumData.getOrDefault(accountName.toLowerCase(), 0L);
	}

	public void addPremiumTime(String accountName, int timeValue, TimeUnit timeUnit)
	{
		long addTime = timeUnit.toMillis(timeValue);
		long now = System.currentTimeMillis();
		long oldPremiumExpiration = Math.max(now, this.getPremiumExpiration(accountName));
		long newPremiumExpiration = oldPremiumExpiration + addTime;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("REPLACE INTO account_premium (account_name,enddate) VALUE (?,?)");)
		{
			stmt.setString(1, accountName.toLowerCase());
			stmt.setLong(2, newPremiumExpiration);
			stmt.execute();
		}
		catch (SQLException var20)
		{
			LOGGER.warning("Problem with PremiumManager: " + var20.getMessage());
		}

		this._premiumData.put(accountName.toLowerCase(), newPremiumExpiration);

		for (Player player : World.getInstance().getPlayers())
		{
			if (accountName.equalsIgnoreCase(player.getAccountName()))
			{
				this.stopExpireTask(player);
				this.startExpireTask(player, newPremiumExpiration - now);
				if (!player.hasPremiumStatus())
				{
					player.setPremiumStatus(true);
				}
				break;
			}
		}
	}

	public void removePremiumStatus(String accountName, boolean checkOnline)
	{
		if (checkOnline)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (accountName.equalsIgnoreCase(player.getAccountName()) && player.hasPremiumStatus())
				{
					player.setPremiumStatus(false);
					this.stopExpireTask(player);
					break;
				}
			}
		}

		this._premiumData.remove(accountName.toLowerCase());

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("DELETE FROM account_premium WHERE account_name = ?");)
		{
			stmt.setString(1, accountName.toLowerCase());
			stmt.execute();
		}
		catch (SQLException var11)
		{
			LOGGER.warning("Problem with PremiumManager: " + var11.getMessage());
		}
	}

	public static PremiumManager getInstance()
	{
		return PremiumManager.SingletonHolder.INSTANCE;
	}

	class PremiumExpireTask implements Runnable
	{
		final Player _player;

		PremiumExpireTask(Player player)
		{
			Objects.requireNonNull(PremiumManager.this);
			super();
			this._player = player;
		}

		@Override
		public void run()
		{
			this._player.setPremiumStatus(false);
		}
	}

	private static class SingletonHolder
	{
		protected static final PremiumManager INSTANCE = new PremiumManager();
	}
}
