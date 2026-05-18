package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SetupGauge extends ServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	private final int _dat1;
	private final int _time;
	private final int _time2;
	private final int _objectId;

	public SetupGauge(int objectId, int dat1, int time)
	{
		this._objectId = objectId;
		this._dat1 = dat1;
		this._time = time;
		this._time2 = time;
	}

	public SetupGauge(int objectId, int color, int currentTime, int maxTime)
	{
		this._objectId = objectId;
		this._dat1 = color;
		this._time = currentTime;
		this._time2 = maxTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SETUP_GAUGE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._dat1);
		buffer.writeInt(this._time);
		buffer.writeInt(this._time2);
	}
}
