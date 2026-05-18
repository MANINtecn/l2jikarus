/*
 * Copyright (c) 2013 L2jBAN-JDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package handlers.targethandlers.affectscope;

import java.util.function.Consumer;

import net.sf.l2jdev.gameserver.handler.AffectObjectHandler;
import net.sf.l2jdev.gameserver.handler.IAffectObjectHandler;
import net.sf.l2jdev.gameserver.handler.IAffectScopeHandler;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;
import net.sf.l2jdev.gameserver.util.LocationUtil;

/**
 * @author Nik, BAN-JDEV
 */
public class SummonExceptMaster implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature creature, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		final int affectRange = skill.getAffectRange();
		final int affectLimit = skill.getAffectLimit();
		if (target.isPlayable())
		{
			int count = 0;
			final int limit = (affectLimit > 0) ? affectLimit : Integer.MAX_VALUE;
			for (Creature c : target.asPlayer().getServitorsAndPets())
			{
				if (c.isDead() || ((affectRange > 0) && !LocationUtil.checkIfInRange(affectRange, c, target, true)))
				{
					continue;
				}

				if ((affectObject != null) && !affectObject.checkAffectedObject(creature, c))
				{
					continue;
				}

				count++;
				action.accept(c);

				if (count >= limit)
				{
					break;
				}
			}
		}
	}

	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.SUMMON_EXCEPT_MASTER;
	}
}
