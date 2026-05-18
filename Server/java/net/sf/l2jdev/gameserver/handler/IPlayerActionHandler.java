package net.sf.l2jdev.gameserver.handler;

import net.sf.l2jdev.gameserver.model.ActionDataHolder;
import net.sf.l2jdev.gameserver.model.actor.Player;

public interface IPlayerActionHandler
{
	void onAction(Player var1, ActionDataHolder var2, boolean var3, boolean var4);

	default boolean isPetAction()
	{
		return false;
	}
}
