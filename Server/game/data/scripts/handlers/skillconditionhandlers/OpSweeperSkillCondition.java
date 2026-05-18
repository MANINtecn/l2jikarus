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

import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

/**
 * @author Zoey76
 */
public class OpSweeperSkillCondition implements ISkillCondition
{
	public OpSweeperSkillCondition(StatSet params)
	{
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		boolean canSweep = false;
		if (caster.isPlayer() && (skill != null))
		{
			final Player sweeper = caster.asPlayer();
			for (WorldObject wo : skill.getTargetsAffected(sweeper, target))
			{
				if ((wo != null) && wo.isAttackable())
				{
					final Attackable attackable = wo.asAttackable();
					if (attackable.isDead())
					{
						if (attackable.isSpoiled())
						{
							canSweep = attackable.checkSpoilOwner(sweeper, true);
							if (canSweep)
							{
								canSweep = !attackable.isOldCorpse(sweeper, NpcConfig.CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY, true);
							}

							if (canSweep)
							{
								canSweep = sweeper.getInventory().checkInventorySlotsAndWeight(attackable.getSpoilLootItems(), true, true);
							}
						}
						else
						{
							sweeper.sendPacket(SystemMessageId.THE_SWEEPER_HAS_FAILED_AS_THE_TARGET_IS_NOT_SPOILED);
						}
					}
				}
			}
		}

		return canSweep;
	}
}
