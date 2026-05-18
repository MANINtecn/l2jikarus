package net.sf.l2jdev.gameserver.model.cubic.conditions;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.Cubic;

public interface ICubicCondition
{
	boolean test(Cubic var1, Creature var2, WorldObject var3);
}
