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
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureDamageReceived;
import net.sf.l2jdev.gameserver.model.events.listeners.FunctionEventListener;
import net.sf.l2jdev.gameserver.model.events.returns.DamageReturn;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class SphericBarrier extends AbstractStatAddEffect
{
	public SphericBarrier(StatSet params)
	{
		super(params, Stat.SPHERIC_BARRIER_RANGE);
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.addListener(new FunctionEventListener(effected, EventType.ON_CREATURE_DAMAGE_RECEIVED, (OnCreatureDamageReceived event) -> onDamageReceivedEvent(event), this));
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_DAMAGE_RECEIVED, listener -> listener.getOwner() == this);
	}

	private DamageReturn onDamageReceivedEvent(OnCreatureDamageReceived event)
	{
		if (event.getAttacker().calculateDistance3D(event.getTarget()) > _amount)
		{
			return new DamageReturn(false, true, false, 0);
		}

		return new DamageReturn(false, false, false, event.getDamage());
	}
}
