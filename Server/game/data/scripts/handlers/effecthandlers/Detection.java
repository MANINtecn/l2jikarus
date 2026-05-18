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
package handlers.effecthandlers;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * Detection effect implementation.
 * @author UnAfraid
 */
public class Detection extends AbstractEffect
{
	public Detection(StatSet params)
	{
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effector.isPlayer() || !effected.isPlayer())
		{
			return;
		}

		final Player player = effector.asPlayer();
		final Player target = effected.asPlayer();
		final boolean hasParty = player.isInParty();
		final boolean hasClan = player.getClanId() > 0;
		final boolean hasAlly = player.getAllyId() > 0;

		if (target.isInvisible())
		{
			if (hasParty && (target.isInParty()) && (player.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()))
			{
				return;
			}
			else if (hasClan && (player.getClanId() == target.getClanId()))
			{
				return;
			}
			else if (hasAlly && (player.getAllyId() == target.getAllyId()))
			{
				return;
			}

			// Remove Hide.
			target.getEffectList().stopEffects(AbnormalType.HIDE);
		}
	}
}
