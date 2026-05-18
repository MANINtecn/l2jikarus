package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ConditionPlayerCanCreateBase extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanCreateBase(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Player player = effector.asPlayer();
			boolean canCreateBase = true;
			Clan clan = player.getClan();
			if (player.isAlikeDead() || player.isCursedWeaponEquipped() || clan == null)
			{
				canCreateBase = false;
			}

			Castle castle = CastleManager.getInstance().getCastle(player);
			Fort fort = FortManager.getInstance().getFort(player);
			if (castle == null && fort == null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
				sm.addSkillName(skill);
				player.sendPacket(sm);
				canCreateBase = false;
			}
			else if ((castle == null || castle.getSiege().isInProgress()) && (fort == null || fort.getSiege().isInProgress()))
			{
				if ((castle == null || castle.getSiege().getAttackerClan(clan) != null) && (fort == null || fort.getSiege().getAttackerClan(clan) != null))
				{
					if (!player.isClanLeader())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
						sm.addSkillName(skill);
						player.sendPacket(sm);
						canCreateBase = false;
					}
					else if ((castle == null || castle.getSiege().getAttackerClan(clan).getNumFlags() < SiegeManager.getInstance().getFlagMaxCount()) && (fort == null || fort.getSiege().getAttackerClan(clan).getNumFlags() < FortSiegeManager.getInstance().getFlagMaxCount()))
					{
						if (!player.isInsideZone(ZoneId.HQ))
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_BUILD_HEADQUARTERS_HERE);
							canCreateBase = false;
						}
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
						sm.addSkillName(skill);
						player.sendPacket(sm);
						canCreateBase = false;
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
					sm.addSkillName(skill);
					player.sendPacket(sm);
					canCreateBase = false;
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
				sm.addSkillName(skill);
				player.sendPacket(sm);
				canCreateBase = false;
			}

			return this._value == canCreateBase;
		}
		return !this._value;
	}
}
