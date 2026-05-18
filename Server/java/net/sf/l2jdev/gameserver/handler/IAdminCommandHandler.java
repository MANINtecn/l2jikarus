package net.sf.l2jdev.gameserver.handler;

import net.sf.l2jdev.gameserver.model.actor.Player;

public interface IAdminCommandHandler
{
	boolean onCommand(String var1, Player var2);

	String[] getCommandList();
}
