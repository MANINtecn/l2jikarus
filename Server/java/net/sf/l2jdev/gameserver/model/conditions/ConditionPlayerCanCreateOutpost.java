package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionPlayerCanCreateOutpost extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanCreateOutpost(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Player player = effector.asPlayer();
			boolean canCreateOutpost = true;
			if (player.isAlikeDead() || player.isCursedWeaponEquipped() || player.getClan() == null)
			{
				canCreateOutpost = false;
			}

			Castle castle = CastleManager.getInstance().getCastle(player);
			Fort fort = FortManager.getInstance().getFort(player);
			if (castle == null && fort == null)
			{
				canCreateOutpost = false;
			}

			if ((fort == null || fort.getResidenceId() != 0) && (castle == null || castle.getResidenceId() != 0))
			{
				if ((fort == null || fort.getZone().isActive()) && (castle == null || castle.getZone().isActive()))
				{
					if (!player.isClanLeader())
					{
						player.sendMessage("You must be a clan leader to construct an outpost or flag.");
						canCreateOutpost = false;
					}
					else if (!player.isInsideZone(ZoneId.HQ))
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BUILD_HEADQUARTERS_HERE);
						canCreateOutpost = false;
					}
				}
				else
				{
					player.sendMessage("You can only construct an outpost or flag on siege field.");
					canCreateOutpost = false;
				}
			}
			else
			{
				player.sendMessage("You must be on fort or castle ground to construct an outpost or flag.");
				canCreateOutpost = false;
			}

			return this._value == canCreateOutpost;
		}
		return !this._value;
	}
}
