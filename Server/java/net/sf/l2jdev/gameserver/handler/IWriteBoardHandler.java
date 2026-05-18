package net.sf.l2jdev.gameserver.handler;

import net.sf.l2jdev.gameserver.model.actor.Player;

public interface IWriteBoardHandler extends IParseBoardHandler
{
	boolean writeCommunityBoardCommand(Player var1, String var2, String var3, String var4, String var5, String var6);
}
