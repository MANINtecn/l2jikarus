package net.sf.l2jdev.gameserver.handler;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;

public interface ITargetTypeHandler
{
	WorldObject getTarget(Creature var1, WorldObject var2, Skill var3, boolean var4, boolean var5, boolean var6);

	Enum<TargetType> getTargetType();
}
