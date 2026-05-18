package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.holders.SharedTeleportHolder;
import net.sf.l2jdev.gameserver.managers.SharedTeleportManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestSharedLocationTeleport extends ClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		this._id = (this.readInt() - 1) / 256;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			SharedTeleportHolder teleport = SharedTeleportManager.getInstance().getTeleport(this._id);
			if (teleport != null && teleport.getCount() != 0)
			{
				if (player.getName().equals(teleport.getName()))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_YOURSELF);
				}
				else if (player.getInventory().getInventoryItemCount(91663, -1) < GeneralConfig.TELEPORT_SHARE_LOCATION_COST)
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
				}
				else if (player.getMovieHolder() == null && !player.isFishing() && !player.isInInstance() && !player.isOnEvent() && !player.isInOlympiadMode() && !player.inObserverMode() && !player.isInTraingCamp() && !player.isInTimedHuntingZone() && !player.isInsideZone(ZoneId.SIEGE))
				{
					if (player.destroyItemByItemId(ItemProcessType.FEE, 91663, GeneralConfig.TELEPORT_SHARE_LOCATION_COST, player, true))
					{
						teleport.decrementCount();
						player.abortCast();
						player.stopMove(null);
						player.teleToLocation(teleport.getLocation());
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.TELEPORTATION_LIMIT_FOR_THE_COORDINATES_RECEIVED_IS_REACHED);
			}
		}
	}
}
