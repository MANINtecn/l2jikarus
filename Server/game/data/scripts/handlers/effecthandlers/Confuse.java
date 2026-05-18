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

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Formulas;

/**
 * Confuse effect implementation.
 * @author littlecrow
 */
public class Confuse extends AbstractEffect
{
	private final int _chance;

	public Confuse(StatSet params)
	{
		_chance = params.getInt("chance", 100);
	}

	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return Formulas.calcProbability(_chance, effector, effected, skill);
	}

	@Override
	public long getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.getAI().notifyAction(Action.CONFUSED);

		final List<Creature> targetList = new ArrayList<>();
		// Getting the possible targets

		World.getInstance().forEachVisibleObject(effected, Creature.class, targetList::add);

		// if there is no target, exit function
		if (!targetList.isEmpty())
		{
			// Choosing randomly a new target
			final Creature target = targetList.get(Rnd.get(targetList.size()));

			// Attacking the target
			effected.setTarget(target);
			effected.getAI().setIntention(Intention.ATTACK, target);
		}
	}
}
