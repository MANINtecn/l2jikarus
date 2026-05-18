package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.sql.OfflineTraderTable;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;

public class Logout extends ClientPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		Player player = client.getPlayer();
		if (player == null)
		{
			client.disconnect();
		}
		else
		{
			if (OlympiadManager.getInstance().isRegistered(player))
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
			}

			Location location = null;
			Instance world = player.getInstanceWorld();
			if (world != null)
			{
				if (GeneralConfig.RESTORE_PLAYER_INSTANCE)
				{
					player.getVariables().set("INSTANCE_RESTORE", world.getId());
				}
				else
				{
					location = world.getExitLocation(player);
					if (location == null)
					{
						location = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
					}
				}

				player.setInstance(null);
			}
			else if (player.isInTimedHuntingZone())
			{
				location = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
			}

			if (location != null)
			{
				player.getVariables().set("RESTORE_LOCATION", location.getX() + ";" + location.getY() + ";" + location.getZ());
			}

			if (OfflineTraderTable.getInstance().enteredOfflineMode(player))
			{
				LOGGER_ACCOUNTING.info("Entered offline mode, " + client);
			}
			else
			{
				Disconnection.of(client, player).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			}
		}
	}
}
