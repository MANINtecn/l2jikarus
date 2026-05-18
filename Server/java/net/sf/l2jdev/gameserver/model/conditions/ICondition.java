package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public interface ICondition
{
	boolean test(Creature var1, WorldObject var2);
}
