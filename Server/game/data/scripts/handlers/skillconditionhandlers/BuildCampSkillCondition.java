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
package handlers.skillconditionhandlers;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Sdw
 */
public class BuildCampSkillCondition implements ISkillCondition
{
	public BuildCampSkillCondition(StatSet params)
	{
	}

	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if ((caster == null) || !caster.isPlayer())
		{
			return false;
		}

		final Player player = caster.asPlayer();
		boolean canCreateBase = true;
		final Clan clan = player.getClan();
		if (player.isAlikeDead() || player.isCursedWeaponEquipped() || (clan == null))
		{
			canCreateBase = false;
		}

		final Castle castle = CastleManager.getInstance().getCastle(player);
		final Fort fort = FortManager.getInstance().getFort(player);
		final SystemMessage sm;
		if ((castle == null) && (fort == null))
		{
			sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			canCreateBase = false;
		}
		else if (((castle != null) && !castle.getSiege().isInProgress()) || ((fort != null) && !fort.getSiege().isInProgress()))
		{
			sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			canCreateBase = false;
		}
		else if (((castle != null) && (castle.getSiege().getAttackerClan(clan) == null)) || ((fort != null) && (fort.getSiege().getAttackerClan(clan) == null)))
		{
			sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			canCreateBase = false;
		}
		else if (!player.isClanLeader())
		{
			sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			canCreateBase = false;
		}
		else if (((castle != null) && (castle.getSiege().getAttackerClan(clan).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())) || ((fort != null) && (fort.getSiege().getAttackerClan(clan).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount())))
		{
			sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			canCreateBase = false;
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_BUILD_HEADQUARTERS_HERE);
			canCreateBase = false;
		}

		return canCreateBase;
	}
}
