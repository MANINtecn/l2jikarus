package net.sf.l2jdev.gameserver.network.clientpackets.huntingzones;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneExit;

public class ExTimedHuntingZoneLeave extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isInCombat())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_TELEPORT_IN_BATTLE));
			}
			else
			{
				TimedHuntingZoneHolder huntingZone = player.getTimedHuntingZone();
				if (huntingZone != null)
				{
					Location exitLocation = huntingZone.getExitLocation();
					if (exitLocation != null)
					{
						player.teleToLocation(exitLocation, null);
					}
					else
					{
						Instance world = player.getInstanceWorld();
						if (world != null)
						{
							world.ejectPlayer(player);
						}
						else
						{
							player.teleToLocation(TeleportWhereType.TOWN);
						}
					}

					ThreadPool.schedule(() -> player.sendPacket(new TimedHuntingZoneExit(huntingZone.getZoneId())), 3000L);
				}
			}
		}
	}
}
