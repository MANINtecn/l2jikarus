package net.sf.l2jdev.gameserver.network.serverpackets.huntingzones;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class TimedHuntingZoneChargeResult extends ServerPacket
{
	private final int _zoneId;
	private final int _remainTime;
	private final int _refillTime;
	private final int _chargeTime;

	public TimedHuntingZoneChargeResult(int zoneId, int remainTime, int refillTime, int chargeTime)
	{
		this._zoneId = zoneId;
		this._remainTime = remainTime;
		this._refillTime = refillTime;
		this._chargeTime = chargeTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TIME_RESTRICT_FIELD_USER_CHARGE_RESULT.writeId(this, buffer);
		buffer.writeInt(this._zoneId);
		buffer.writeInt(this._remainTime);
		buffer.writeInt(this._refillTime);
		buffer.writeInt(this._chargeTime);
	}
}
