package net.sf.l2jdev.gameserver.handler;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.actor.Player;

public interface IParseBoardHandler
{
	Logger LOG = Logger.getLogger(IParseBoardHandler.class.getName());

	boolean onCommand(String var1, Player var2);

	String[] getCommandList();
}
