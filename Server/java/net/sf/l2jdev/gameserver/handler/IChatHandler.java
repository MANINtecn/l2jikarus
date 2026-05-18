package net.sf.l2jdev.gameserver.handler;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public interface IChatHandler
{
	void onChat(ChatType var1, Player var2, String var3, String var4, boolean var5);

	ChatType[] getChatTypeList();
}
