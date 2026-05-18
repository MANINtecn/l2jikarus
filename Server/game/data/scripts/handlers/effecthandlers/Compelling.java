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
package handlers.effecthandlers;

import net.sf.l2jdev.gameserver.ai.Action;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Formulas;

/**
 * @author BAN-JDEV
 */
public class Compelling extends AbstractEffect
{
	public Compelling(StatSet params)
	{
	}

	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return Formulas.calcProbability(100, effector, effected, skill);
	}

	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		if ((effected == null) || effected.isRaid() || (effected.isNpc() && !effected.isAttackable()))
		{
			return false;
		}

		return effected.isPlayable() || effected.isAttackable();
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.getAI().notifyAction(Action.AFRAID);
		compellingAction(effector, effected);
	}

	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill, Item item)
	{
		compellingAction(null, effected);
		return false;
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (!effected.isPlayer())
		{
			effected.getAI().notifyAction(Action.THINK);
		}
	}

	private static void compellingAction(Creature effector, Creature effected)
	{
		effected.setRunning();
		effected.getAI().setIntention(Intention.MOVE_TO, effector.getLocation());
	}
}
