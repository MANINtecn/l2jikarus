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
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * @author UnAfraid
 */
public class TargetMyPartySkillCondition implements ISkillCondition
{
	private final boolean _includeMe;

	public TargetMyPartySkillCondition(StatSet params)
	{
		_includeMe = params.getBoolean("includeMe");
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if ((target == null) || !target.isPlayable())
		{
			return false;
		}

		final Party party = caster.getParty();
		if (target.isPlayer())
		{
			final Party targetParty = target.asPlayer().getParty();
			return ((party == null) ? (_includeMe && (caster == target)) : (_includeMe ? party == targetParty : (party == targetParty) && (caster != target)));
		}
		else if (target.isSummon())
		{
			final Summon summon = target.asSummon();
			final Player summonOwner = summon.getOwner();
			if (summonOwner != null)
			{
				final Party targetParty = summonOwner.getParty();
				return ((party == null) ? (_includeMe && (caster == summonOwner)) : (_includeMe ? party == targetParty : (party == targetParty) && (caster != summonOwner)));
			}
		}

		return false;
	}
}
