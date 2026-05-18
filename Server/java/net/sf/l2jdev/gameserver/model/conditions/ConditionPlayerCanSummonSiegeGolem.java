package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSummonSiegeGolem extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanSummonSiegeGolem(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Player player = effector.asPlayer();
			boolean canSummonSiegeGolem = true;
			if (player.isAlikeDead() || player.isCursedWeaponEquipped() || player.getClan() == null)
			{
				canSummonSiegeGolem = false;
			}

			Castle castle = CastleManager.getInstance().getCastle(player);
			Fort fort = FortManager.getInstance().getFort(player);
			if (castle == null && fort == null)
			{
				canSummonSiegeGolem = false;
			}

			if ((fort == null || fort.getResidenceId() != 0) && (castle == null || castle.getResidenceId() != 0))
			{
				if ((castle == null || castle.getSiege().isInProgress()) && (fort == null || fort.getSiege().isInProgress()))
				{
					if (player.getClanId() != 0 && (castle != null && castle.getSiege().getAttackerClan(player.getClanId()) == null || fort != null && fort.getSiege().getAttackerClan(player.getClanId()) == null))
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
						canSummonSiegeGolem = false;
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.INVALID_TARGET);
					canSummonSiegeGolem = false;
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				canSummonSiegeGolem = false;
			}

			return this._value == canSummonSiegeGolem;
		}
		return !this._value;
	}
}
