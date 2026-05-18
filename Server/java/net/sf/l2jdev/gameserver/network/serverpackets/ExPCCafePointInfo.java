package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExPCCafePointInfo extends ServerPacket
{
	private final int _points;
	private final int _mAddPoint;
	private final int _mPeriodType;
	private final int _remainTime;
	private final int _pointType;
	private final int _time;

	public ExPCCafePointInfo()
	{
		this._points = 0;
		this._mAddPoint = 0;
		this._remainTime = 0;
		this._mPeriodType = 0;
		this._pointType = 0;
		this._time = 0;
	}

	public ExPCCafePointInfo(int points, int pointsToAdd, int time)
	{
		this._points = points;
		this._mAddPoint = pointsToAdd;
		this._mPeriodType = 1;
		this._remainTime = 0;
		this._pointType = pointsToAdd < 0 ? 2 : 1;
		this._time = time;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PCCAFE_POINT_INFO.writeId(this, buffer);
		buffer.writeInt(this._points);
		buffer.writeInt(this._mAddPoint);
		buffer.writeByte(this._mPeriodType);
		buffer.writeInt(this._remainTime);
		buffer.writeByte(this._pointType);
		buffer.writeInt(this._time * 3);
	}
}
