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
package ai.others.OrcFortress;

import java.util.HashSet;
import java.util.Set;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Script;

/**
 * @author Serenitty
 */
public class RunnersOrcs extends Script
{
	// NPCs
	private static final Set<Integer> ORC_GUARDS = new HashSet<>();
	static
	{
		ORC_GUARDS.add(22175);
		ORC_GUARDS.add(22176);
		ORC_GUARDS.add(22177);
		ORC_GUARDS.add(22178);
		ORC_GUARDS.add(22179);
		ORC_GUARDS.add(22180);
		ORC_GUARDS.add(22181);

	}
	private static final Set<Integer> ORC_GUARDS_ALL = new HashSet<>();
	static
	{
		ORC_GUARDS_ALL.add(22164);
		ORC_GUARDS_ALL.add(22165);
		ORC_GUARDS_ALL.add(22166);
		ORC_GUARDS_ALL.add(22167);
		ORC_GUARDS_ALL.add(22168);
		ORC_GUARDS_ALL.add(22169);
		ORC_GUARDS_ALL.add(22170);
		ORC_GUARDS_ALL.add(22171);
		ORC_GUARDS_ALL.add(22172);
		ORC_GUARDS_ALL.add(22173);
		ORC_GUARDS_ALL.add(22174);
		ORC_GUARDS_ALL.add(22175);
		ORC_GUARDS_ALL.add(22176);
		ORC_GUARDS_ALL.add(22177);
		ORC_GUARDS_ALL.add(22178);
		ORC_GUARDS_ALL.add(22179);
		ORC_GUARDS_ALL.add(22180);
		ORC_GUARDS_ALL.add(22181);

	}
	private static final int ORC_FORTRESS_FLAG = 18397;

	public RunnersOrcs()
	{
		addSpawnId(ORC_GUARDS);
		addCreatureSeeId(ORC_GUARDS_ALL);
	}

	@Override
	public void onSpawn(Npc npc)
	{
		World.getInstance().forEachVisibleObject(npc, Npc.class, flag -> {
			if ((flag.getId() == ORC_FORTRESS_FLAG))
			{
				npc.setRunning();
				npc.asAttackable().addDamageHate(flag, 0, 999999);
				npc.getAI().setIntention(Intention.ATTACK, flag);
			}
		});
	}

	@Override
	public void onCreatureSee(Npc npc, Creature creature)
	{
		if (creature.isPlayer())
		{
			World.getInstance().forEachVisibleObjectInRange(npc, Player.class, 800, players -> {
				npc.setRunning();
				npc.asAttackable().addDamageHate(players, 0, 999999);
				npc.getAI().setIntention(Intention.ATTACK, players);
			});
		}
	}

	public static void main(String[] args)
	{
		new RunnersOrcs();
	}
}
