package net.sf.l2jdev.gameserver.model.cubic;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.cubic.conditions.ICubicCondition;

public interface ICubicConditionHolder
{
	boolean validateConditions(Cubic var1, Creature var2, WorldObject var3);

	void addCondition(ICubicCondition var1);
}
