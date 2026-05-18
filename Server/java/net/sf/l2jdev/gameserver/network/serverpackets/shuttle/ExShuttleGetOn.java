package net.sf.l2jdev.gameserver.network.serverpackets.shuttle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShuttleGetOn extends ServerPacket
{
	private final int _playerObjectId;
	private final int _shuttleObjectId;
	private final Location _pos;

	public ExShuttleGetOn(Player player, Shuttle shuttle)
	{
		this._playerObjectId = player.getObjectId();
		this._shuttleObjectId = shuttle.getObjectId();
		this._pos = player.getInVehiclePosition();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GETON_SHUTTLE.writeId(this, buffer);
		buffer.writeInt(this._playerObjectId);
		buffer.writeInt(this._shuttleObjectId);
		buffer.writeInt(this._pos.getX());
		buffer.writeInt(this._pos.getY());
		buffer.writeInt(this._pos.getZ());
	}
}
