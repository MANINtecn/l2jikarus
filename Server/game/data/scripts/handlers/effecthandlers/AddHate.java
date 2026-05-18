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
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * Add Hate effect implementation.
 * @author Adry_85
 */
public class AddHate extends AbstractEffect
{
	private final double _power;
	private final boolean _affectSummoner;

	public AddHate(StatSet params)
	{
		_power = params.getDouble("power", 0);
		_affectSummoner = params.getBoolean("affectSummoner", false);

		if (params.contains("amount"))
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " should use power instead of amount.");
		}
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature creature, Creature effected, Skill skill, Item item)
	{
		Creature effector = creature;
		if (_affectSummoner && (effector.getSummoner() != null))
		{
			effector = effector.getSummoner();
		}

		if (!effected.isAttackable())
		{
			return;
		}

		final double val = _power;
		if (val > 0)
		{
			effected.asAttackable().addDamageHate(effector, 0, (int) val);
			effected.setRunning();
		}
		else if (val < 0)
		{
			effected.asAttackable().reduceHate(effector, (int) -val);
		}
	}
}
