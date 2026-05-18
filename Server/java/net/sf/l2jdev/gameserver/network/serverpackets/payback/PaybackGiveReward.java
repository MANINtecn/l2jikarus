package net.sf.l2jdev.gameserver.network.serverpackets.payback;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class PaybackGiveReward extends ServerPacket
{
	private final boolean _status;
	private final int _eventID;
	private final int _index;

	public PaybackGiveReward(boolean status, int eventID, int index)
	{
		this._status = status;
		this._eventID = eventID;
		this._index = index;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PAYBACK_GIVE_REWARD.writeId(this, buffer);
		buffer.writeByte(this._status);
		buffer.writeByte(this._eventID);
		buffer.writeInt(this._index);
	}
}
