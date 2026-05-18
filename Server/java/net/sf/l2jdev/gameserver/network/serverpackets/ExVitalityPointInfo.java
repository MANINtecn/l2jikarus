package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExVitalityPointInfo extends ServerPacket
{
	private final int _vitalityPoints;

	public ExVitalityPointInfo(int vitPoints)
	{
		this._vitalityPoints = vitPoints;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VITALITY_POINT_INFO.writeId(this, buffer);
		buffer.writeInt(this._vitalityPoints);
	}
}
