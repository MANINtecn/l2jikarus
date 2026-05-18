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
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * Hp By Level effect implementation.
 * @author Zoey76
 */
public class HpByLevel extends AbstractEffect
{
	private final double _power;

	public HpByLevel(StatSet params)
	{
		_power = params.getDouble("power", 0);

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
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		// Calculation
		final double abs = _power;
		final double absorb = ((effector.getCurrentHp() + abs) > effector.getMaxHp() ? effector.getMaxHp() : (effector.getCurrentHp() + abs));
		final int restored = (int) (absorb - effector.getCurrentHp());
		effector.setCurrentHp(absorb);

		// System message
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_RECOVERED_S1_HP);
		sm.addInt(restored);
		effector.sendPacket(sm);
	}
}
