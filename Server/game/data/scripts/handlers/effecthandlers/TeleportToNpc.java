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

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.FlyType;
import net.sf.l2jdev.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2jdev.gameserver.network.serverpackets.ValidateLocation;

/**
 * Teleport player/party to summoned npc effect implementation.
 * @author Nik
 */
public class TeleportToNpc extends AbstractEffect
{
	private final int _npcId;
	private final boolean _party;

	public TeleportToNpc(StatSet params)
	{
		_npcId = params.getInt("npcId");
		_party = params.getBoolean("party", false);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT_TO_TARGET;
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		ILocational teleLocation = null;
		for (Npc npc : effector.getSummonedNpcs())
		{
			if (npc.getId() == _npcId)
			{
				teleLocation = npc;
			}
		}

		if (teleLocation != null)
		{
			final Party party = effected.getParty();
			if (_party && (party != null))
			{
				for (Player member : party.getMembers())
				{
					teleport(member, teleLocation);
				}
			}
			else
			{
				teleport(effected, teleLocation);
			}
		}
	}

	private static void teleport(Creature effected, ILocational location)
	{
		if (effected.isInsideRadius2D(location, 900))
		{
			effected.getAI().setIntention(Intention.IDLE);
			effected.broadcastPacket(new FlyToLocation(effected, location, FlyType.DUMMY));
			effected.abortAttack();
			effected.abortCast();
			effected.setXYZ(location);
			effected.broadcastPacket(new ValidateLocation(effected));
			effected.revalidateZone(true);
		}
		else
		{
			effected.teleToLocation(location);
		}
	}
}
