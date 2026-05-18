package net.sf.l2jdev.gameserver.model.actor.tasks.player;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.IllegalActionPunishmentType;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentAffect;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentTask;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;

public class IllegalPlayerActionTask implements Runnable
{
	private static final Logger AUDIT_LOGGER = Logger.getLogger("audit");
	private final String _message;
	private final IllegalActionPunishmentType _punishment;
	private final Player _actor;

	public IllegalPlayerActionTask(Player actor, String message, IllegalActionPunishmentType punishment)
	{
		this._message = message;
		this._punishment = punishment;
		this._actor = actor;
		switch (punishment)
		{
			case KICK:
				this._actor.sendMessage("You will be kicked for illegal action, GM informed.");
				break;
			case KICKBAN:
				if (!this._actor.isGM())
				{
					this._actor.setAccessLevel(-1, false, true);
					this._actor.setAccountAccesslevel(-1);
				}

				this._actor.sendMessage("You are banned for illegal action, GM informed.");
				break;
			case JAIL:
				this._actor.sendMessage("Illegal action performed!");
				this._actor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
		}
	}

	@Override
	public void run()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("AUDIT, ");
		sb.append(this._message);
		sb.append(", ");
		sb.append(this._actor);
		sb.append(", ");
		sb.append(this._punishment);
		AUDIT_LOGGER.info(sb.toString());
		if (!this._actor.isGM())
		{
			switch (this._punishment)
			{
				case KICK:
					Disconnection.of(this._actor).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
					break;
				case KICKBAN:
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(this._actor.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN, System.currentTimeMillis() + GeneralConfig.DEFAULT_PUNISH_PARAM * 1000L, this._message, this.getClass().getSimpleName()));
					break;
				case JAIL:
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(this._actor.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + GeneralConfig.DEFAULT_PUNISH_PARAM * 1000L, this._message, this.getClass().getSimpleName()));
					break;
				case BROADCAST:
					AdminData.getInstance().broadcastMessageToGMs(this._message);
					return;
			}
		}
	}
}
