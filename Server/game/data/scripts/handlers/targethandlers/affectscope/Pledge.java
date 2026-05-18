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
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;

/**
 * @author Nik
 */
public class Pledge implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature creature, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		final int affectRange = skill.getAffectRange();
		final int affectLimit = skill.getAffectLimit();
		if (target.isPlayable())
		{
			final Playable playable = target.asPlayable();
			final Player player = playable.asPlayer();
			final int clanId = player.getClanId();

			// Create the target filter.
			final AtomicInteger affected = new AtomicInteger(0);
			final Predicate<Playable> filter = c -> {
				if ((affectLimit > 0) && (affected.get() >= affectLimit))
				{
					return false;
				}

				final Player p = c.asPlayer();
				if ((p == null) || p.isDead())
				{
					return false;
				}

				if (p != player)
				{
					final int pClanId = p.getClanId();
					if ((pClanId == 0) || (pClanId != clanId))
					{
						return false;
					}
				}

				if ((affectObject != null) && !affectObject.checkAffectedObject(creature, p))
				{
					return false;
				}

				affected.incrementAndGet();
				return true;
			};

			// Add object of origin since it is skipped in the forEachVisibleObjectInRange method.
			if (filter.test(playable))
			{
				action.accept(playable);
			}

			// Check and add targets.
			World.getInstance().forEachVisibleObjectInRange(playable, Playable.class, affectRange, c -> {
				if (filter.test(c))
				{
					action.accept(c);
				}
			});
		}
	}

	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.PLEDGE;
	}
}
