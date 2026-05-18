package net.sf.l2jdev.gameserver.handler;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;

public interface IBypassHandler
{
	Logger LOGGER = Logger.getLogger(IBypassHandler.class.getName());

	boolean onCommand(String var1, Player var2, Creature var3);

	String[] getCommandList();
}
