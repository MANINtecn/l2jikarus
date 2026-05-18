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
import java.util.List;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillConditionAffectType;

/**
 * @author UnAfraid, BAN-JDEV
 */
public class OpCheckClassListSkillCondition implements ISkillCondition
{
	private final Set<PlayerClass> _classIds = new HashSet<>();
	private final SkillConditionAffectType _affectType;
	private final boolean _isWithin;

	public OpCheckClassListSkillCondition(StatSet params)
	{
		final List<PlayerClass> classIds = params.getEnumList("classIds", PlayerClass.class);
		if (classIds != null)
		{
			_classIds.addAll(classIds);
		}

		_affectType = params.getEnum("affectType", SkillConditionAffectType.class);
		_isWithin = params.getBoolean("isWithin");
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				return caster.isPlayer() && (_classIds.contains(caster.asPlayer().getPlayerClass()) == _isWithin);
			}
			case TARGET:
			{
				return (target != null) && target.isPlayer() && (_classIds.contains(target.asPlayer().getPlayerClass()) == _isWithin);
			}
			default:
			{
				return false;
			}
		}
	}
}
