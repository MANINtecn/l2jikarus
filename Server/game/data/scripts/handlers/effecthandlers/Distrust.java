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

import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.AttackableAI;
import net.sf.l2jdev.gameserver.ai.DistrustAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.instance.Chest;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

/**
 * @author Naker
 */
public class Distrust extends AbstractEffect
{
	public Distrust(StatSet params)
	{
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected.isDead() || !effected.isMonster() || (effected instanceof Chest) || effected.isRaid() || effected.isRaidMinion())
		{
			effector.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}

		final Monster targetMonster = effected.asMonster();
		final List<Monster> targets = World.getInstance().getVisibleObjectsInRange(targetMonster, Monster.class, 1100, m -> (m != targetMonster) && !m.isDead() && m.isAttackable() && !(m instanceof Chest) && !m.isRaid() && !m.isRaidMinion());
		if (targets.isEmpty())
		{
			return;
		}

		final Monster newTarget = targets.get(Rnd.get(targets.size()));
		if ((newTarget == null) || (newTarget == effected))
		{
			return;
		}

		targetMonster.setAI(new DistrustAI(effected.asAttackable(), newTarget));
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		final Attackable monster = effected.asAttackable();
		if ((monster == null) || monster.isDead())
		{
			return;
		}

		monster.setAI(new AttackableAI(monster));
		monster.setTarget(null);
		monster.setWalking();
		monster.getAI().setIntention(Intention.ACTIVE);
	}
}
