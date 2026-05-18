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
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Sdw
 */
public class OpResurrectionSkillCondition implements ISkillCondition
{
	public OpResurrectionSkillCondition(StatSet params)
	{
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		boolean canResurrect = true;
		if (target == caster)
		{
			return canResurrect;
		}

		if (target == null)
		{
			return false;
		}

		if (target.isPlayer())
		{
			final Player player = target.asPlayer();
			if (!player.isDead())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
					msg.addSkillName(skill);
					caster.sendPacket(msg);
				}
			}
			else if (player.isResurrectionBlocked())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				}
			}
			else if (player.isReviveRequested())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				}
			}
		}
		else if (target.isSummon())
		{
			final Summon summon = target.asSummon();
			final Player player = target.asPlayer();
			if (!summon.isDead())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
					msg.addSkillName(skill);
					caster.sendPacket(msg);
				}
			}
			else if (summon.isResurrectionBlocked())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				}
			}
			else if ((player != null) && player.isRevivingPet())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				}
			}
		}

		return canResurrect;
	}
}
