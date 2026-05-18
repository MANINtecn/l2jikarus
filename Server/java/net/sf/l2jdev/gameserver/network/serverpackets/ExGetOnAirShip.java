package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExGetOnAirShip extends ServerPacket
{
	private final int _playerId;
	private final int _airShipId;
	private final Location _pos;

	public ExGetOnAirShip(Player player, Creature ship)
	{
		this._playerId = player.getObjectId();
		this._airShipId = ship.getObjectId();
		this._pos = player.getInVehiclePosition();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GETON_AIRSHIP.writeId(this, buffer);
		buffer.writeInt(this._playerId);
		buffer.writeInt(this._airShipId);
		buffer.writeInt(this._pos.getX());
		buffer.writeInt(this._pos.getY());
		buffer.writeInt(this._pos.getZ());
	}
}
