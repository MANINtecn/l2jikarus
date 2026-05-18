package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.Siege;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ConditionPlayerCanResurrect extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanResurrect(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (skill.getAffectRange() > 0)
		{
			return true;
		}
		else if (effected == null)
		{
			return false;
		}
		else
		{
			boolean canResurrect = true;
			if (effected.isPlayer())
			{
				Player player = effected.asPlayer();
				if (!player.isDead())
				{
					canResurrect = false;
					if (effector.isPlayer())
					{
						SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
						msg.addSkillName(skill);
						effector.sendPacket(msg);
					}
				}
				else if (player.isResurrectionBlocked())
				{
					canResurrect = false;
					if (effector.isPlayer())
					{
						effector.sendPacket(SystemMessageId.REJECT_RESURRECTION);
					}
				}
				else if (player.isReviveRequested())
				{
					canResurrect = false;
					if (effector.isPlayer())
					{
						effector.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
					}
				}
				else if (skill.getId() != 2393)
				{
					Siege siege = SiegeManager.getInstance().getSiege(player);
					if (siege != null && siege.isInProgress())
					{
						Clan clan = player.getClan();
						if (clan == null)
						{
							canResurrect = false;
							if (effector.isPlayer())
							{
								effector.sendPacket(SystemMessageId.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEGROUNDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
							}
						}
						else if (siege.checkIsDefender(clan) && siege.getControlTowerCount() == 0)
						{
							canResurrect = false;
							if (effector.isPlayer())
							{
								effector.sendPacket(SystemMessageId.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
							}
						}
						else if (siege.checkIsAttacker(clan) && siege.getAttackerClan(clan).getNumFlags() == 0)
						{
							canResurrect = false;
							if (effector.isPlayer())
							{
								effector.sendPacket(SystemMessageId.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
							}
						}
						else
						{
							canResurrect = false;
							if (effector.isPlayer())
							{
								effector.sendPacket(SystemMessageId.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEGROUNDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
							}
						}
					}
				}
			}
			else if (effected.isSummon())
			{
				Summon summon = effected.asSummon();
				Player player = summon.getOwner();
				if (!summon.isDead())
				{
					canResurrect = false;
					if (effector.isPlayer())
					{
						SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
						msg.addSkillName(skill);
						effector.sendPacket(msg);
					}
				}
				else if (summon.isResurrectionBlocked())
				{
					canResurrect = false;
					if (effector.isPlayer())
					{
						effector.sendPacket(SystemMessageId.REJECT_RESURRECTION);
					}
				}
				else if (player != null && player.isRevivingPet())
				{
					canResurrect = false;
					if (effector.isPlayer())
					{
						effector.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
					}
				}
			}

			return this._value == canResurrect;
		}
	}
}
