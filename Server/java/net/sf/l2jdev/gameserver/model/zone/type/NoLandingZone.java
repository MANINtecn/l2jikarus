package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MountType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class NoLandingZone extends ZoneType
{
	private int dismountDelay = 5;

	public NoLandingZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("dismountDelay"))
		{
			this.dismountDelay = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.NO_LANDING, true);
			Player player = creature.asPlayer();
			if (player.isGM())
			{
				player.sendMessage("You have entered a no-landing zone. Dismount restrictions are ignored for GMs.");
				return;
			}

			if (player.getMountType() == MountType.WYVERN)
			{
				player.sendPacket(SystemMessageId.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN_YOU_WILL_BE_DISMOUNTED_FROM_YOUR_WYVERN_IF_YOU_DO_NOT_LEAVE);
				player.enteredNoLanding(this.dismountDelay);
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.NO_LANDING, false);
			Player player = creature.asPlayer();
			if (player.getMountType() == MountType.WYVERN)
			{
				player.exitedNoLanding();
			}
		}
	}
}
