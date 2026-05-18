package net.sf.l2jdev.gameserver.network.serverpackets.prison;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPrisonUserInfo extends ServerPacket
{
	private final int _prisonType;
	private final int _itemAmount;
	private final int _remainTime;

	public ExPrisonUserInfo(int prisonType, int itemAmount, int remainTime)
	{
		this._prisonType = prisonType;
		this._itemAmount = itemAmount;
		this._remainTime = remainTime;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PRISON_USER_INFO.writeId(this, buffer);
		buffer.writeByte(this._prisonType);
		buffer.writeInt(this._itemAmount);
		buffer.writeInt(this._remainTime);
	}
}
