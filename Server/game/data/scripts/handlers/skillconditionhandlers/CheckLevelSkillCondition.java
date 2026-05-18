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
import net.sf.l2jdev.gameserver.model.skill.enums.SkillConditionAffectType;

/**
 * @author UnAfraid
 */
public class CheckLevelSkillCondition implements ISkillCondition
{
	private final int _minLevel;
	private final int _maxLevel;
	private final SkillConditionAffectType _affectType;

	public CheckLevelSkillCondition(StatSet params)
	{
		_minLevel = params.getInt("minLevel", 1);
		_maxLevel = params.getInt("maxLevel", Integer.MAX_VALUE);
		_affectType = params.getEnum("affectType", SkillConditionAffectType.class, SkillConditionAffectType.CASTER);
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				return (caster.getLevel() >= _minLevel) && (caster.getLevel() <= _maxLevel);
			}
			case TARGET:
			{
				if ((target != null) && target.isPlayer())
				{
					final Player player = target.asPlayer();
					return (player.getLevel() >= _minLevel) && (player.getLevel() <= _maxLevel);
				}
				break;
			}
		}

		return false;
	}
}
