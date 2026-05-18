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
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

/**
 * Untargetable effect implementation.
 * @author UnAfraid
 */
public class Untargetable extends AbstractEffect
{
	public Untargetable(StatSet params)
	{
	}

	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return effected.isPlayer();
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		// Remove target from those that have the untargetable creature on target.
		World.getInstance().forEachVisibleObject(effected, Creature.class, c -> {
			if (c.getTarget() == effected)
			{
				c.setTarget(null);
			}
		});
	}

	@Override
	public long getEffectFlags()
	{
		return EffectFlag.UNTARGETABLE.getMask();
	}
}
