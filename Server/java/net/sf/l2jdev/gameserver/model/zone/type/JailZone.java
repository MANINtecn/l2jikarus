package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.tasks.player.TeleportTask;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class JailZone extends ZoneType
{
	private static final Location JAIL_IN_LOC = new Location(-114356, -249645, -2984);
	private static final Location JAIL_OUT_LOC = new Location(17836, 170178, -3507);

	public JailZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.JAIL, true);
			creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			if (GeneralConfig.JAIL_IS_PVP)
			{
				creature.setInsideZone(ZoneId.PVP, true);
				creature.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			}

			if (GeneralConfig.JAIL_DISABLE_TRANSACTION)
			{
				creature.setInsideZone(ZoneId.NO_STORE, true);
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			player.setInsideZone(ZoneId.JAIL, false);
			player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			if (GeneralConfig.JAIL_IS_PVP)
			{
				creature.setInsideZone(ZoneId.PVP, false);
				creature.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
			}

			if (player.isJailed())
			{
				ThreadPool.schedule(new TeleportTask(player, JAIL_IN_LOC), 2000L);
				creature.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
			}

			if (GeneralConfig.JAIL_DISABLE_TRANSACTION)
			{
				creature.setInsideZone(ZoneId.NO_STORE, false);
			}
		}
	}

	public static Location getLocationIn()
	{
		return JAIL_IN_LOC;
	}

	public static Location getLocationOut()
	{
		return JAIL_OUT_LOC;
	}
}
