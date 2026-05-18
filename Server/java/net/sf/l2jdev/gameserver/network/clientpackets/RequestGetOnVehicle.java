package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.BoatManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Boat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.GetOnVehicle;

public class RequestGetOnVehicle extends ClientPacket
{
	private int _boatId;
	private Location _pos;

	@Override
	protected void readImpl()
	{
		this._boatId = this.readInt();
		this._pos = new Location(this.readInt(), this.readInt(), this.readInt());
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Boat boat;
			if (player.isInBoat())
			{
				boat = player.getBoat();
				if (boat.getObjectId() != this._boatId)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else
			{
				boat = BoatManager.getInstance().getBoat(this._boatId);
				if (boat == null || boat.isMoving() || !player.isInsideRadius3D(boat, 1000))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}

			player.setInVehiclePosition(this._pos);
			player.setVehicle(boat);
			player.broadcastPacket(new GetOnVehicle(player.getObjectId(), boat.getObjectId(), this._pos));
			player.setXYZ(boat.getX(), boat.getY(), boat.getZ());
			player.setInsideZone(ZoneId.PEACE, true);
			player.revalidateZone(true);
		}
	}
}
