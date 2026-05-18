package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class ConditionPlayerCanTakeFort extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanTakeFort(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Player player = effector.asPlayer();
			boolean canTakeFort = true;
			if (player.isAlikeDead() || player.isCursedWeaponEquipped() || !player.isClanLeader())
			{
				canTakeFort = false;
			}

			Fort fort = FortManager.getInstance().getFort(player);
			if (fort == null || fort.getResidenceId() <= 0 || !fort.getSiege().isInProgress() || fort.getSiege().getAttackerClan(player.getClan()) == null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
				sm.addSkillName(skill);
				player.sendPacket(sm);
				canTakeFort = false;
			}
			else if (fort.getFlagPole() != effected)
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				canTakeFort = false;
			}
			else if (!LocationUtil.checkIfInRange(200, player, effected, true))
			{
				player.sendPacket(SystemMessageId.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_CANCELLED);
				canTakeFort = false;
			}

			return this._value == canTakeFort;
		}
		return !this._value;
	}
}
