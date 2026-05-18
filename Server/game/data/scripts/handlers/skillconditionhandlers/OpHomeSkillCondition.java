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

import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.residences.ResidenceType;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * @author Sdw
 */
public class OpHomeSkillCondition implements ISkillCondition
{
	private final ResidenceType _type;

	public OpHomeSkillCondition(StatSet params)
	{
		_type = params.getEnum("type", ResidenceType.class);
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if (caster.isPlayer())
		{
			final Clan clan = caster.asPlayer().getClan();
			if (clan != null)
			{
				switch (_type)
				{
					case CASTLE:
					{
						return CastleManager.getInstance().getCastleByOwner(clan) != null;
					}
					case FORTRESS:
					{
						return FortManager.getInstance().getFortByOwner(clan) != null;
					}
					case CLANHALL:
					{
						return ClanHallData.getInstance().getClanHallByClan(clan) != null;
					}
				}
			}
		}

		return false;
	}
}
