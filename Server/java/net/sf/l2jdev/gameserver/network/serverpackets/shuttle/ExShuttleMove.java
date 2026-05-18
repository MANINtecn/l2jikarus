package net.sf.l2jdev.gameserver.network.serverpackets.shuttle;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShuttleMove extends ServerPacket
{
	private final Shuttle _shuttle;
	private final int _x;
	private final int _y;
	private final int _z;

	public ExShuttleMove(Shuttle shuttle, int x, int y, int z)
	{
		this._shuttle = shuttle;
		this._x = x;
		this._y = y;
		this._z = z;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHUTTLE_MOVE.writeId(this, buffer);
		buffer.writeInt(this._shuttle.getObjectId());
		buffer.writeInt((int) this._shuttle.getStat().getMoveSpeed());
		buffer.writeInt((int) this._shuttle.getStat().getRotationSpeed());
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
	}
}
