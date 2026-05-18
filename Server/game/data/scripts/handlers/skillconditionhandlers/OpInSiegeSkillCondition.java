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
package handlers.skillconditionhandlers;

import java.util.HashSet;
import java.util.Set;

import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.siege.FortSiege;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * @author dontknowdontcare
 */
public class OpInSiegeSkillCondition implements ISkillCondition
{
	private final Set<Integer> _residenceIds = new HashSet<>();

	public OpInSiegeSkillCondition(StatSet params)
	{
		_residenceIds.addAll(params.getList("residenceIds", Integer.class));
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		for (int id : _residenceIds)
		{
			if (valid(caster, id))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean valid(Creature caster, int id)
	{
		final FortSiege fortSiege = FortSiegeManager.getInstance().getSiege(id);
		if (fortSiege != null)
		{
			return fortSiege.isInProgress() && fortSiege.getFort().getZone().isInsideZone(caster);
		}

		final Siege castleSiege = SiegeManager.getInstance().getSiege(id);
		if (castleSiege != null)
		{
			return castleSiege.isInProgress() && castleSiege.getCastle().getZone().isInsideZone(caster);
		}

		// TODO: Check for clan hall siege

		return false;
	}
}
