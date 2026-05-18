package net.sf.l2jdev.gameserver.handler;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;

public interface IActionHandler
{
	Logger LOGGER = Logger.getLogger(IActionHandler.class.getName());

	boolean onAction(Player var1, WorldObject var2, boolean var3);

	InstanceType getInstanceType();
}
