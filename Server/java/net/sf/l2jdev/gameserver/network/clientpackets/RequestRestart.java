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
import net.sf.l2jdev.gameserver.network.ConnectionState;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.CharSelectionInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.RestartResponse;

public class RequestRestart extends ClientPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");

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
			if (!player.canLogout())
			{
				player.sendPacket(RestartResponse.FALSE);
				player.sendPacket(ActionFailed.STATIC_PACKET);
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

				GameClient client = this.getClient();
				if (OfflineTraderTable.getInstance().enteredOfflineMode(player))
				{
					LOGGER_ACCOUNTING.info("Entered offline mode, " + client);
				}
				else
				{
					Disconnection.of(client, player).storeAndDelete();
				}

				client.setConnectionState(ConnectionState.AUTHENTICATED);
				client.sendPacket(RestartResponse.TRUE);
				CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
				client.sendPacket(new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
				client.setCharSelection(cl.getCharInfo());
			}
		}
	}
}
