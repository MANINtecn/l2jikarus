package net.sf.l2jdev.gameserver.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentAffect;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentTask;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;
import net.sf.l2jdev.gameserver.network.ConnectionState;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public class FloodProtectorAction
{
	private static final Logger LOGGER = Logger.getLogger(FloodProtectorAction.class.getName());
	private static final boolean LOG_WARNING_ENABLED = LOGGER.isLoggable(Level.WARNING);
 
	private final GameClient _client;
	private final FloodProtectorSettings _settings;
	private final AtomicInteger _requestCount = new AtomicInteger(0);
	private volatile int _nextGameTick = GameTimeTaskManager.getInstance().getGameTicks();
	private volatile boolean _punishmentInProgress;
	private volatile boolean _logged;

	public FloodProtectorAction(GameClient client, FloodProtectorSettings settings)
	{
		this._client = client;
		this._settings = settings;
	}

	public boolean canPerformAction()
	{
		Player player = this._client.getPlayer();
		if (player != null && player.isGM())
		{
			return true;
		}
		int currentTick = GameTimeTaskManager.getInstance().getGameTicks();
		if (currentTick >= this._nextGameTick && !this._punishmentInProgress)
		{
			if (LOG_WARNING_ENABLED && this._settings.isLogFlooding() && this._requestCount.get() > 0)
			{
				StringBuilder sb = this.buildLogPrefix();
				StringUtil.append(sb, " issued ", String.valueOf(this._requestCount.get()), " extra requests within ~", String.valueOf(this._settings.getProtectionInterval() * 100), " ms.");
				LOGGER.warning(sb.toString());
			}

			this._nextGameTick = currentTick + this._settings.getProtectionInterval();
			this._logged = false;
			this._requestCount.set(0);
			return true;
		}
		if (LOG_WARNING_ENABLED && this._settings.isLogFlooding() && !this._logged)
		{
			int timeUntilNext = (this._settings.getProtectionInterval() - (this._nextGameTick - currentTick)) * 100;
			this.logFlooding(timeUntilNext);
			this._logged = true;
		}

		int currentCount = this._requestCount.incrementAndGet();
		if (!this._punishmentInProgress)
		{
			int punishmentLimit = this._settings.getPunishmentLimit();
			if (punishmentLimit > 0 && currentCount >= punishmentLimit)
			{
				String punishmentType = this._settings.getPunishmentType();
				if (punishmentType != null)
				{
					this._punishmentInProgress = true;

					try
					{
						switch (punishmentType)
						{
							case "kick":
								Disconnection.of(this._client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
								if (LOG_WARNING_ENABLED)
								{
									this.logPunishment("kicked for flooding");
								}
								break;
							case "ban":
								long punishmentTimex = this._settings.getPunishmentTime();
								PunishmentManager.getInstance().startPunishment(new PunishmentTask(this._client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN, System.currentTimeMillis() + punishmentTimex, "", this.getClass().getSimpleName()));
								if (LOG_WARNING_ENABLED)
								{
									String duration = punishmentTimex <= 0L ? "forever" : StringUtil.concat("for ", String.valueOf(punishmentTimex / 60000L), " mins.");
									this.logPunishment(StringUtil.concat("banned for flooding ", duration));
								}
								break;
							case "jail":
								long punishmentTime = this._settings.getPunishmentTime();
								if (player != null)
								{
									int characterId = player.getObjectId();
									if (characterId > 0)
									{
										PunishmentManager.getInstance().startPunishment(new PunishmentTask(characterId, PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + punishmentTime, "", this.getClass().getSimpleName()));
									}
								}

								if (LOG_WARNING_ENABLED)
								{
									String duration = punishmentTime <= 0L ? "forever" : StringUtil.concat("for ", String.valueOf(punishmentTime / 60000L), " mins.");
									this.logPunishment(StringUtil.concat("jailed for flooding ", duration));
								}
								break;
							default:
								if (LOG_WARNING_ENABLED)
								{
									LOGGER.warning(StringUtil.concat("FloodProtector: Unknown punishment type configured: ", punishmentType));
								}
						}
					}
					finally
					{
						this._punishmentInProgress = false;
					}
				}
			}
		}

		return false;
	}

	private void logFlooding(int timeUntilNext)
	{
		StringBuilder sb = this.buildLogPrefix();
		StringUtil.append(sb, " called command ", this._settings.getFloodProtectorType(), " ~", String.valueOf(timeUntilNext), " ms after previous command.");
		LOGGER.warning(sb.toString());
	}

	private void logPunishment(String message)
	{
		StringBuilder sb = this.buildLogPrefix();
		StringUtil.append(sb, " ", message);
		LOGGER.warning(sb.toString());
	}

	private StringBuilder buildLogPrefix()
	{
		StringBuilder sb = new StringBuilder(128);
		StringUtil.append(sb, this._settings.getFloodProtectorType(), ": ");
		ConnectionState connectionState = this._client.getConnectionState();
		switch (connectionState)
		{
			case ENTERING:
			case IN_GAME:
				Player player = this._client.getPlayer();
				if (player != null)
				{
					StringUtil.append(sb, player.getName(), "(", String.valueOf(player.getObjectId()), ") ");
				}
				break;
			case AUTHENTICATED:
				String accountName = this._client.getAccountName();
				if (accountName != null)
				{
					StringUtil.append(sb, accountName, " ");
				}
				break;
			case CONNECTED:
				try
				{
					if (!this._client.isDetached())
					{
						String clientAddress = this._client.getIp();
						if (clientAddress != null)
						{
							StringUtil.append(sb, clientAddress);
						}
					}
				}
				catch (Exception var4)
				{
				}
				break;
			default:
				throw new IllegalStateException(StringUtil.concat("FloodProtector: Missing connection state in switch: ", connectionState.toString()));
		}

		return sb;
	}
}
