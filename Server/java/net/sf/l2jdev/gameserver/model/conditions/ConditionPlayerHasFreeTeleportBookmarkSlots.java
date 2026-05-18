package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionPlayerHasFreeTeleportBookmarkSlots extends Condition
{
	private final int _teleportBookmarkSlots;

	public ConditionPlayerHasFreeTeleportBookmarkSlots(int teleportBookmarkSlots)
	{
		this._teleportBookmarkSlots = teleportBookmarkSlots;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		if (player == null)
		{
			return false;
		}
		else if (player.getBookMarkSlot() + this._teleportBookmarkSlots > 18)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_REACHED_THE_MAXIMUM_NUMBER_OF_MY_TELEPORT_SLOTS_OR_USE_CONDITIONS_ARE_NOT_OBSERVED);
			return false;
		}
		else
		{
			return true;
		}
	}
}
