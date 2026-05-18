package net.sf.l2jdev.gameserver.network.serverpackets.orcfortress;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class OrcFortressSiegeInfoHUD extends ServerPacket
{
	private final int _fortressId;
	private final int _siegeState;
	private final int _nowTime;
	private final int _remainTime;

	public OrcFortressSiegeInfoHUD(int fortressId, int siegeState, int nowTime, int remainTime)
	{
		this._fortressId = fortressId;
		this._siegeState = siegeState;
		this._nowTime = nowTime;
		this._remainTime = remainTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADEN_FORTRESS_SIEGE_HUD_INFO.writeId(this, buffer);
		buffer.writeInt(this._fortressId);
		buffer.writeInt(this._siegeState);
		buffer.writeInt(this._nowTime);
		buffer.writeInt(this._remainTime);
	}
}
