package net.sf.l2jdev.gameserver.network.serverpackets.shuttle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShuttleGetOff extends ServerPacket
{
	private final int _playerObjectId;
	private final int _shuttleObjectId;
	private final int _x;
	private final int _y;
	private final int _z;

	public ExShuttleGetOff(Player player, Shuttle shuttle, int x, int y, int z)
	{
		this._playerObjectId = player.getObjectId();
		this._shuttleObjectId = shuttle.getObjectId();
		this._x = x;
		this._y = y;
		this._z = z;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GETOFF_SHUTTLE.writeId(this, buffer);
		buffer.writeInt(this._playerObjectId);
		buffer.writeInt(this._shuttleObjectId);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
	}
}
