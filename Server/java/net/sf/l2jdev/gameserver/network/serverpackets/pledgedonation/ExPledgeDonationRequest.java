package net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeDonationRequest extends ServerPacket
{
	private final boolean _success;
	private final int _type;
	private final int _curPoints;

	public ExPledgeDonationRequest(boolean success, int type, int curPoints)
	{
		this._success = success;
		this._type = type;
		this._curPoints = curPoints;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_DONATION_REQUEST.writeId(this, buffer);
		buffer.writeByte(this._type);
		buffer.writeInt(this._success);
		buffer.writeShort(0);
		buffer.writeInt(3);
		buffer.writeInt(14);
		buffer.writeLong(0L);
		buffer.writeShort(0);
		buffer.writeInt(this._curPoints);
	}
}
