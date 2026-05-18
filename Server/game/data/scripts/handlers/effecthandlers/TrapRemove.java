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
import net.sf.l2jdev.gameserver.model.actor.enums.creature.TrapAction;
import net.sf.l2jdev.gameserver.model.actor.instance.Trap;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnTrapAction;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

/**
 * Trap Remove effect implementation.
 * @author UnAfraid
 */
public class TrapRemove extends AbstractEffect
{
	private final int _power;

	public TrapRemove(StatSet params)
	{
		if (params.isEmpty())
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + ": effect without power!");
		}

		_power = params.getInt("power");
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isTrap() || effected.isAlikeDead())
		{
			return;
		}

		final Trap trap = (Trap) effected;
		if (!trap.canBeSeen(effector))
		{
			if (effector.isPlayer())
			{
				effector.sendPacket(SystemMessageId.INVALID_TARGET);
			}
			return;
		}

		if (trap.getLevel() > _power)
		{
			return;
		}

		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_TRAP_ACTION, trap))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnTrapAction(trap, effector, TrapAction.TRAP_DISARMED), trap);
		}

		trap.unSummon();
		if (effector.isPlayer())
		{
			effector.sendPacket(SystemMessageId.THE_TRAP_DEVICE_HAS_BEEN_STOPPED);
		}
	}
}
