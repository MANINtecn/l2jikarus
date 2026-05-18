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
package handlers.skillconditionhandlers;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.EffectScope;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;

import handlers.effecthandlers.SummonCubic;

/**
 * @author UnAfraid
 */
public class CanSummonCubicSkillCondition implements ISkillCondition
{
	public CanSummonCubicSkillCondition(StatSet params)
	{
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if (!caster.isPlayer() || caster.isAlikeDead())
		{
			return false;
		}

		final Player player = caster.asPlayer();
		if (player.inObserverMode())
		{
			return false;
		}

		if (player.getAutoUseSettings().isAutoSkill(skill.getId()))
		{
			for (AbstractEffect effect : skill.getEffects(EffectScope.GENERAL))
			{
				if ((effect instanceof SummonCubic) && (player.getCubicById(((SummonCubic) effect).getCubicId()) != null))
				{
					return false;
				}
			}
		}

		return !player.inObserverMode() && !player.isMounted() && !player.isSpawnProtected() && !player.isTeleportProtected();
	}
}
