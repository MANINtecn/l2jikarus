package net.sf.l2jdev.gameserver.handler;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.actor.Player;

public interface IUserCommandHandler
{
	Logger LOGGER = Logger.getLogger(IUserCommandHandler.class.getName());

	boolean onCommand(int var1, Player var2);

	int[] getCommandList();
}
