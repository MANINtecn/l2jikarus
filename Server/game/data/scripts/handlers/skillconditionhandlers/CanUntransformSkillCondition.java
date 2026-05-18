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
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

/**
 * @author Sdw
 */
public class CanUntransformSkillCondition implements ISkillCondition
{
	public CanUntransformSkillCondition(StatSet params)
	{
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		boolean canUntransform = true;
		final Player player = caster.asPlayer();
		if (player == null)
		{
			canUntransform = false;
		}
		else if (player.isAlikeDead() || player.isCursedWeaponEquipped())
		{
			canUntransform = false;
		}
		else if (player.isFlyingMounted() && !player.isInsideZone(ZoneId.LANDING))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_TOO_HIGH_TO_PERFORM_THIS_ACTION_PLEASE_LOWER_YOUR_ALTITUDE_AND_TRY_AGAIN); // TODO: check if message is retail like.
			canUntransform = false;
		}

		return canUntransform;
	}
}
