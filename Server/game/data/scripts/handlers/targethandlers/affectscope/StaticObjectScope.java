/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.targethandlers.affectscope;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.sf.l2jdev.gameserver.handler.AffectObjectHandler;
import net.sf.l2jdev.gameserver.handler.IAffectObjectHandler;
import net.sf.l2jdev.gameserver.handler.IAffectScopeHandler;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.StaticObject;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;

/**
 * Static Object affect scope implementation. Used to detect hidden doors.
 * @author Nik
 */
public class StaticObjectScope implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature creature, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		final int affectRange = skill.getAffectRange();
		final int affectLimit = skill.getAffectLimit();

		// Target checks.
		final AtomicInteger affected = new AtomicInteger(0);
		final Predicate<Creature> filter = c -> {
			if (((affectLimit > 0) && (affected.get() >= affectLimit)) || c.isDead())
			{
				return false;
			}

			if (!c.isDoor() && !(c instanceof StaticObject))
			{
				return false;
			}

			if ((affectObject != null) && !affectObject.checkAffectedObject(creature, c))
			{
				return false;
			}

			affected.incrementAndGet();
			return true;
		};

		// Add object of origin since it is skipped in the forEachVisibleObjectInRange method.
		if (target.isCreature() && filter.test(target.asCreature()))
		{
			action.accept(target);
		}

		// Check and add targets.
		World.getInstance().forEachVisibleObjectInRange(target, Creature.class, affectRange, c -> {
			if (filter.test(c))
			{
				action.accept(c);
			}
		});
	}

	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.STATIC_OBJECT_SCOPE;
	}
}
