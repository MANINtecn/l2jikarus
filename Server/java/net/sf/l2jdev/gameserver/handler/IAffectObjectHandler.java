package net.sf.l2jdev.gameserver.handler;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectObject;

public interface IAffectObjectHandler
{
	boolean checkAffectedObject(Creature var1, Creature var2);

	Enum<AffectObject> getAffectObjectType();
}
