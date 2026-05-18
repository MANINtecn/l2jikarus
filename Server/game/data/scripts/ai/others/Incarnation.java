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
package ai.others;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.ListenerRegisterType;
import net.sf.l2jdev.gameserver.model.events.annotations.Id;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterEvent;
import net.sf.l2jdev.gameserver.model.events.annotations.RegisterType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureSkillFinishCast;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcSpawn;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.script.Script;

/**
 * @author Nik
 */
public class Incarnation extends Script
{
	@RegisterEvent(EventType.ON_NPC_SPAWN)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(13302)
	@Id(13303)
	@Id(13304)
	@Id(13305)
	@Id(13455)
	@Id(13456)
	@Id(13457)
	public void onNpcSpawn(OnNpcSpawn event)
	{
		final Npc npc = event.getNpc();
		if (npc.getSummoner() != null)
		{
			npc.getSummoner().addListener(new ConsumerEventListener(npc, EventType.ON_CREATURE_ATTACK, (OnCreatureAttack e) -> onOffense(npc, e.getAttacker(), e.getTarget()), this));
			npc.getSummoner().addListener(new ConsumerEventListener(npc, EventType.ON_CREATURE_SKILL_FINISH_CAST, (OnCreatureSkillFinishCast e) -> onOffense(npc, e.getCaster(), e.getTarget()), this));
		}
	}

	public void onOffense(Npc npc, Creature attacker, WorldObject target)
	{
		if ((attacker == target) || (npc.getSummoner() == null))
		{
			return;
		}

		// Attack target of summoner
		npc.setRunning();
		npc.getAI().setIntention(Intention.ATTACK, target);
	}

	public static void main(String[] args)
	{
		new Incarnation();
	}
}
