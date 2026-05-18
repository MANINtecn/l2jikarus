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
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.StatModifierType;
import net.sf.l2jdev.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class AbstractStatAddEffect extends AbstractEffect
{
	private final Stat _stat;
	protected final double _amount;

	public AbstractStatAddEffect(StatSet params, Stat stat)
	{
		_stat = stat;
		_amount = params.getDouble("amount", 0);

		if (params.getEnum("mode", StatModifierType.class, StatModifierType.DIFF) != StatModifierType.DIFF)
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " can only use DIFF mode.");
		}

		if (params.contains("power"))
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " should use amount instead of power.");
		}
	}

	@Override
	public void pump(Creature effected, Skill skill)
	{
		effected.getStat().mergeAdd(_stat, _amount);
	}
}
