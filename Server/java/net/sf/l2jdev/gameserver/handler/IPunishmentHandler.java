package net.sf.l2jdev.gameserver.handler;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.punishment.PunishmentTask;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;

public interface IPunishmentHandler
{
	Logger LOGGER = Logger.getLogger(IPunishmentHandler.class.getName());

	void onStart(PunishmentTask var1);

	void onEnd(PunishmentTask var1);

	PunishmentType getType();
}
