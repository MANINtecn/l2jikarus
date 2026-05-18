package net.sf.l2jdev.gameserver.network.serverpackets.shuttle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExValidateLocationInShuttle extends ServerPacket
{
	private final Player _player;
	private final int _shipId;
	private final int _heading;
	private final Location _loc;

	public ExValidateLocationInShuttle(Player player)
	{
		this._player = player;
		this._shipId = this._player.getShuttle().getObjectId();
		this._loc = player.getInVehiclePosition();
		this._heading = player.getHeading();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VALIDATE_LOCATION_IN_SHUTTLE.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._shipId);
		buffer.writeInt(this._loc.getX());
		buffer.writeInt(this._loc.getY());
		buffer.writeInt(this._loc.getZ());
		buffer.writeInt(this._heading);
	}
}
