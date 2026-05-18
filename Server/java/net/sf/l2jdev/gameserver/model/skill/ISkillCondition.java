package net.sf.l2jdev.gameserver.model.skill;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;

public interface ISkillCondition
{
	boolean canUse(Creature var1, Skill var2, WorldObject var3);
}
