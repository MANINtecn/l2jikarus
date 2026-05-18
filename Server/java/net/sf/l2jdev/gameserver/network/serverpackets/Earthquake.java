package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Earthquake extends ServerPacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _intensity;
	private final int _duration;

	public Earthquake(ILocational location, int intensity, int duration)
	{
		this._x = location.getX();
		this._y = location.getY();
		this._z = location.getZ();
		this._intensity = intensity;
		this._duration = duration;
	}

	public Earthquake(int x, int y, int z, int intensity, int duration)
	{
		this._x = x;
		this._y = y;
		this._z = z;
		this._intensity = intensity;
		this._duration = duration;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EARTHQUAKE.writeId(this, buffer);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(this._intensity);
		buffer.writeInt(this._duration);
		buffer.writeInt(0);
	}
}
