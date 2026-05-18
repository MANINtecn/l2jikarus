package net.sf.l2jdev.gameserver.handler;

import java.util.function.Consumer;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;

public interface IAffectScopeHandler
{
	void forEachAffected(Creature var1, WorldObject var2, Skill var3, Consumer<? super WorldObject> var4);

	Enum<AffectScope> getAffectScopeType();
}
