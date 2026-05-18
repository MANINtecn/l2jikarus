package net.sf.l2jdev.gameserver.handler;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public interface IItemHandler
{
	Logger LOGGER = Logger.getLogger(IItemHandler.class.getName());

	boolean onItemUse(Playable var1, Item var2, boolean var3);
}
